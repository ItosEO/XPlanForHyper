package com.itos.xplanforhyper.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itos.xplanforhyper.R
import com.itos.xplanforhyper.datatype.AppInfo
import com.itos.xplanforhyper.datatype.UninstallMethod
import com.itos.xplanforhyper.utils.OLog
import com.itos.xplanforhyper.utils.OPackage
import com.itos.xplanforhyper.utils.OShizuku
import com.itos.xplanforhyper.utils.SpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.itos.xplanforhyper.datatype.ShizukuResult
import com.itos.xplanforhyper.utils.OData
import java.io.BufferedReader
import java.io.InputStreamReader
import android.provider.Settings

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _pkglist = mutableStateOf<List<AppInfo>>(emptyList())
    val pkglist: List<AppInfo> get() = _pkglist.value

    private val _optlist = mutableStateOf<List<AppInfo>>(emptyList())
    val optlist: List<AppInfo> get() = _optlist.value

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: Boolean get() = _isRefreshing.value

    private val _uninstallMethod = MutableStateFlow(UninstallMethod.PM_ENHANCED)
    val uninstallMethod: StateFlow<UninstallMethod> = _uninstallMethod.asStateFlow()
    
    private val _terminalResult = MutableStateFlow<ShizukuResult?>(null)
    val terminalResult: StateFlow<ShizukuResult?> = _terminalResult.asStateFlow()

    init {
        loadAppLists()
        loadUninstallMethod()
        checkAndGrantSecureSettingsPermission()
    }

    private fun checkAndGrantSecureSettingsPermission() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            try {
                Settings.Secure.putString(context.contentResolver, "xplan", "test")
                OData.is_have_premissipn = true
            } catch (e: Exception) {
                OLog.e("写入安全设置权限异常", e)
                // Assuming Shizuku is available and authorized. This logic might need refinement
                // based on how Shizuku's status is exposed from the Activity to ViewModel.
                val result = OShizuku.exec("pm grant com.itos.xplanforhyper android.permission.WRITE_SECURE_SETTINGS".toByteArray())
                if (result.exitCode == 0 && result.output.isBlank()) {
                    OData.is_have_premissipn = true
                } else {
                    OData.is_have_premissipn = false
                    OLog.i("设置 写入安全设置权限异常", result.output)
                }
            }
        }
    }

    private fun loadUninstallMethod() {
        val methodValue = SpUtils.getParam(getApplication(), "method", UninstallMethod.PM_ENHANCED.value) as Int
        _uninstallMethod.value = UninstallMethod.fromValue(methodValue)
    }

    fun setUninstallMethod(method: UninstallMethod) {
        _uninstallMethod.value = method
        SpUtils.setParam(getApplication(), "method", method.value)
    }
    
    fun executeTerminalCommand(command: String) {
        viewModelScope.launch {
            _terminalResult.value = OShizuku.exec(command.toByteArray())
        }
    }
    
    fun resetTerminalResult() {
        _terminalResult.value = null
    }

    fun uninstallApp(appInfo: AppInfo) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.PM_ENHANCED -> "pm uninstall --user 0 ${appInfo.appPkg}"
                UninstallMethod.PM -> "pm uninstall ${appInfo.appPkg}"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 ${appInfo.appPkg} i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 ${appInfo.appPkg} i32 0 i32 0"
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("卸载结果", result)
                refreshAppList()
            }
        }
    }

    fun reinstallApp(appInfo: AppInfo) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 ${appInfo.appPkg} i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 ${appInfo.appPkg} i32 1 i32 0"
                else -> "pm install-existing ${appInfo.appPkg}"
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "请稍等...", Toast.LENGTH_LONG).show()
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("重装结果", result)
                refreshAppList()
            }
        }
    }

    fun uninstallPackageByName(packageName: String) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.PM_ENHANCED -> "pm uninstall --user 0 $packageName"
                UninstallMethod.PM -> "pm uninstall $packageName"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 $packageName i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 $packageName i32 0 i32 0"
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("卸载结果", result)
                refreshAppList()
            }
        }
    }

    fun reinstallPackageByName(packageName: String) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 $packageName i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 $packageName i32 1 i32 0"
                else -> "pm install-existing $packageName"
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "请稍等...", Toast.LENGTH_LONG).show()
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("重装结果", result)
                refreshAppList()
            }
        }
    }
    
    private fun showResultDialog(title: String, result: ShizukuResult) {
        val message = result.output.ifBlank { "操作完成，无返回信息。" }
        val finalTitle = if (result.exitCode != 0) "$title (错误码: ${result.exitCode})" else title
        
        MaterialAlertDialogBuilder(getApplication())
            .setTitle(finalTitle)
            .setMessage(message)
            .setPositiveButton("好") { _, _ ->
                refreshAppList()
            }
            .show()
    }

    private fun loadAppLists() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            try {
                // Load pkglist
                val pkglistResult = mutableListOf<AppInfo>()
                val inputStreamPkg = context.resources.openRawResource(R.raw.pkglist)
                BufferedReader(InputStreamReader(inputStreamPkg)).forEachLine { line ->
                    val packageName = line.trim()
                    if (packageName.isNotBlank()) {
                        pkglistResult.add(AppInfo(appName = "", appPkg = packageName))
                    }
                }
                _pkglist.value = pkglistResult

                // Load optlist
                val optlistResult = mutableListOf<AppInfo>()
                val inputStreamOpt = context.resources.openRawResource(R.raw.optlist)
                BufferedReader(InputStreamReader(inputStreamOpt)).forEachLine { line ->
                    val packageName = line.trim()
                    if (packageName.isNotBlank()) {
                        optlistResult.add(AppInfo(appName = "", appPkg = packageName))
                    }
                }
                _optlist.value = optlistResult

                // After loading base lists, generate full details
                generateAppListDetails()

            } catch (e: Exception) {
                OLog.e("loadAppLists failed", e)
            }
        }
    }

    fun refreshAppList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            generateAppListDetails()
            _isRefreshing.value = false
        }
    }

    private fun generateAppListDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val packageManager = context.packageManager

            val updatedPkgList = _pkglist.value.map { appInfo ->
                updateAppInfo(appInfo, context, packageManager)
            }
            _pkglist.value = updatedPkgList

            val updatedOptList = _optlist.value.map { appInfo ->
                updateAppInfo(appInfo, context, packageManager)
            }
            _optlist.value = updatedOptList

            OLog.i("列表项", "ViewModel App lists updated")
        }
    }

    private fun updateAppInfo(appInfo: AppInfo, context: Context, packageManager: PackageManager): AppInfo {
        val isInstalled = OPackage.isInstalled(appInfo.appPkg, packageManager)
        return if (isInstalled) {
            appInfo.copy(
                appName = getAppNameByPackageName(context, appInfo.appPkg),
                isDisabled = isAppDisabled(appInfo.appPkg, packageManager),
                isExist = true,
                appIcon = OPackage.getAppIconByPackageName(appInfo.appPkg, packageManager)
            )
        } else {
            appInfo.copy(
                appName = "未安装",
                isExist = false,
                appIcon = null
            )
        }
    }

    private fun getAppNameByPackageName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val applicationInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "未安装"
        }
    }

    private fun isAppDisabled(appPackageName: String, packageManager: PackageManager): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(appPackageName, 0)
            packageInfo.applicationInfo?.let { !it.enabled } ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun patchProcessLimit() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            withContext(Dispatchers.Main) {
                MaterialAlertDialogBuilder(context)
                    .setTitle("关闭缓存进程和虚进程数量限制")
                    .setMessage("该操作可能导致卡米，您确定要进行吗？")
                    .setPositiveButton("确定") { _, _ ->
                        Toast.makeText(context, "请稍等...", Toast.LENGTH_LONG).show()
                        viewModelScope.launch {
                            OShizuku.exec("device_config set_sync_disabled_for_tests persistent;device_config put activity_manager max_cached_processes 2007;device_config put activity_manager max_phantom_processes 2007;echo success".toByteArray())
                            withContext(Dispatchers.Main) {
                                MaterialAlertDialogBuilder(context)
                                    .setTitle("关闭缓存进程和虚进程数量限制")
                                    .setMessage("调整完成，是否立即重启")
                                    .setPositiveButton("立即重启") { _, _ ->
                                        viewModelScope.launch {
                                            OShizuku.exec("reboot".toByteArray())
                                        }
                                    }
                                    .setNegativeButton("暂不重启", null)
                                    .show()
                            }
                        }
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }

    fun unpatchProcessLimit() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "请稍等...", Toast.LENGTH_LONG).show()
                viewModelScope.launch {
                    OShizuku.exec("device_config set_sync_disabled_for_tests none;device_config put activity_manager max_cached_processes 32;device_config put activity_manager max_phantom_processes 32".toByteArray())
                    withContext(Dispatchers.Main) {
                        MaterialAlertDialogBuilder(context)
                            .setTitle("还原缓存进程和虚进程数量限制")
                            .setMessage("还原完成，是否立即重启")
                            .setPositiveButton("立即重启") { _, _ ->
                                viewModelScope.launch {
                                    OShizuku.exec("reboot".toByteArray())
                                }
                            }
                            .setNegativeButton("暂不重启", null)
                            .show()
                    }
                }
            }
        }
    }

    fun hideHD() {
        viewModelScope.launch {
            OLog.i("隐藏HD", "Shizuku方案")
            val result = OShizuku.exec("settings get secure icon_blacklist".toByteArray())
            var data = result.output
            OLog.i("隐藏HD", "当前黑名单列表: $data")
            data = data.trimEnd()
            data = data.replace(Regex(",+"), ",")
            data = data.replace(Regex("(,rotate,hd)+"), ",rotate,hd")
            data = "$data,rotate,hd"
            OLog.i("隐藏HD", "处理后黑名单列表: $data")
            OShizuku.exec("settings put secure icon_blacklist $data,rotate,hd".toByteArray())
        }
    }

    fun unhideHD() {
        viewModelScope.launch {
            OLog.i("还原HD", "Shizuku方案")
            var data = OShizuku.exec("settings get secure icon_blacklist".toByteArray()).output
            data = data.trimEnd()

            OLog.i("还原HD", "待处理数据: $data")
            val targets = listOf("hd")
            val regex = targets.joinToString(separator = "|").toRegex()
            var resultString = regex.replace(data, "")
            resultString = resultString.replace(Regex("(,rotate)+"), ",rotate")
            resultString = resultString.replace(Regex(",+"), ",")
            OLog.i("还原HD", "处理结果: $resultString")

            OShizuku.exec("settings put secure icon_blacklist $resultString".toByteArray())
        }
    }

    fun settingsOpt() {
        viewModelScope.launch {
            val result = OShizuku.exec(OData.configdata.shell.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("系统参数调优", result)
            }
        }
    }

    fun settingsRestore() {
        viewModelScope.launch {
            val result = OShizuku.exec(OData.configdata.restore.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("还原系统参数", result)
            }
        }
    }
} 