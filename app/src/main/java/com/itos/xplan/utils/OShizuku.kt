package com.itos.xplan.utils

import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.InputEvent
import android.view.KeyEvent
import android.widget.Toast
import com.itos.xplan.BuildConfig
import com.itos.xplan.XPlanForHyper.Companion.app
import com.itos.xplan.datatype.ShizukuResult
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuRemoteProcess
import rikka.shizuku.SystemServiceHelper
import java.util.concurrent.Executors

object OShizuku {
    private var isExecuting = false
    private val myUserId = android.os.Process.myUserHandle().hashCode()
    private val isRoot get() = Shizuku.getUid() == 0
    private val userId get() = if (isRoot) myUserId else 0
    private val callerPackage get() = if (isRoot) BuildConfig.APPLICATION_ID else "com.android.shell"

    private fun asInterface(className: String, serviceName: String): Any =
        ShizukuBinderWrapper(SystemServiceHelper.getSystemService(serviceName)).let {
            Class.forName("$className\$Stub").run {
                if (target(Build.VERSION_CODES.P)) HiddenApiBypass.invoke(
                    this,
                    null,
                    "asInterface",
                    it
                )
                else getMethod("asInterface", IBinder::class.java).invoke(null, it)
            }
        }

    private fun target(api: Int): Boolean = Build.VERSION.SDK_INT >= api
    val lockScreen
        get() = runCatching {
            val input = asInterface("android.hardware.input.IInputManager", "input")
            val inject = input::class.java.getMethod(
                "injectInputEvent", InputEvent::class.java, Int::class.java
            )
            val now = SystemClock.uptimeMillis()
            inject.invoke(
                input, KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_POWER, 0), 0
            )
            inject.invoke(
                input, KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_POWER, 0), 0
            )
            true
        }.getOrElse {
            false
        }

    private fun forceStopApp(packageName: String) = runCatching {
        asInterface("android.app.IActivityManager", "activity").let {
            if (target(Build.VERSION_CODES.P)) HiddenApiBypass.invoke(
                it::class.java, it, "forceStopPackage", packageName, userId
            ) else it::class.java.getMethod(
                "forceStopPackage", String::class.java, Int::class.java
            ).invoke(
                it, packageName, userId
            )
        }
        true
    }.getOrElse {
        false
    }

    fun setAppDisabled(packageName: String, disabled: Boolean){

        if (disabled) forceStopApp(packageName)
        runCatching {
            val pm = asInterface("android.content.pm.IPackageManager", "package")
            val newState = when {
                !disabled -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                isRoot -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
            }
            pm::class.java.getMethod(
                "setApplicationEnabledSetting",
                String::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java,
                String::class.java
            ).invoke(pm, packageName, newState, 0, myUserId, BuildConfig.APPLICATION_ID)
        }.onFailure {
        }

    }

    fun setAppHidden(packageName: String, hidden: Boolean): Boolean {
        if (hidden) forceStopApp(packageName)
        return runCatching {
            val pm = asInterface("android.content.pm.IPackageManager", "package")
            pm::class.java.getMethod(
                "setApplicationHiddenSettingAsUser",
                String::class.java,
                Boolean::class.java,
                Int::class.java
            ).invoke(pm, packageName, hidden, userId) as Boolean
        }.getOrElse {
            false
        }
    }

     fun checkShizuku() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) Shizuku.requestPermission(
                0
            ) else app.c = true
        } catch (e: java.lang.Exception) {
            if (app.checkSelfPermission("moe.shizuku.manager.permission.API_V23") == PackageManager.PERMISSION_GRANTED) app.c =
                true
            if (e.javaClass == IllegalStateException::class.java) {
                app.b = false
            }
        }
        if (!app.b || !app.c) {
            Toast.makeText(
                app,
                "Shizuku " + (if (app.b) "已运行" else "未运行") + if (app.c) " 已授权" else " 未授权",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private val ParcelFileDescriptor.text
        get() = ParcelFileDescriptor.AutoCloseInputStream(this)
            .use { it.bufferedReader().readText() }

    fun exec(cmd: ByteArray): ShizukuResult {
        if (isExecuting) {
            return ShizukuResult(-1, "正在执行其他操作")
        }

        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            return ShizukuResult(-1, "Shizuku未授权")
        }

        isExecuting = true
        var process: ShizukuRemoteProcess? = null
        val executor = Executors.newFixedThreadPool(2)
        try {
            process = Shizuku.newProcess(arrayOf("sh"), null, null)
            process.outputStream.use { outputStream ->
                outputStream.write(cmd)
            }

            val output = executor.submit<String> {
                process.inputStream.bufferedReader().readText()
            }
            val error = executor.submit<String> {
                process.errorStream.bufferedReader().readText()
            }

            val exitCode = process.waitFor()
            val resultOutput = output.get()
            val errorOutput = error.get()

            OLog.i("运行shell", "Output_Normal:\n$resultOutput")
            OLog.i("运行shell", "Output_Error:\n$errorOutput")

            return ShizukuResult(exitCode, resultOutput + errorOutput)
        } catch (e: Exception) {
            OLog.e("ShizukuExec Error", e)
            return ShizukuResult(-1, e.message ?: "Unknown error")
        } finally {
            executor.shutdownNow()
            process?.destroy()
            isExecuting = false
        }
    }
}