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
import com.kongzue.dialogx.dialogs.MessageDialog
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel

/**
 * 应用列表的 ViewModel，负责处理数据加载、应用操作等逻辑。
 * @param application 应用实例，用于访问应用上下文。
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * 从 `pkglist` 文件加载的应用列表。
     */
    private val _pkglist = mutableStateOf<List<AppInfo>>(emptyList())
    val pkglist: List<AppInfo> get() = _pkglist.value

    /**
     * 从 `optlist` 文件加载的应用列表（通常用于优化）。
     */
    private val _optlist = mutableStateOf<List<AppInfo>>(emptyList())
    val optlist: List<AppInfo> get() = _optlist.value

    /**
     * 指示应用列表是否正在刷新。
     */
    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: Boolean get() = _isRefreshing.value

    /**
     * 当前选择的应用卸载方法。
     */
    private val _uninstallMethod = MutableStateFlow(UninstallMethod.PM_ENHANCED)
    val uninstallMethod: StateFlow<UninstallMethod> = _uninstallMethod.asStateFlow()

    /**
     * 保存终端命令执行的结果。
     */
    private val _terminalResult = MutableStateFlow<ShizukuResult?>(null)
    val terminalResult: StateFlow<ShizukuResult?> = _terminalResult.asStateFlow()

    init {
        OLog.i("AppViewModel", "ViewModel created")
        loadAppLists()
        loadUninstallMethod()
        checkAndGrantSecureSettingsPermission()
    }

    /**
     * 检查并尝试授予 `WRITE_SECURE_SETTINGS` 权限。
     * 首先会尝试直接写入，如果失败，则通过 Shizuku 授予权限。
     */
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

    /**
     * 从 SharedPreferences 加载用户保存的卸载方法。
     */
    private fun loadUninstallMethod() {
        val methodValue = SpUtils.getParam(getApplication(), "method", UninstallMethod.PM_ENHANCED.value) as Int
        _uninstallMethod.value = UninstallMethod.fromValue(methodValue)
    }

    /**
     * 设置并持久化用户选择的卸载方法。
     * @param method 用户选择的卸载方法。
     */
    fun setUninstallMethod(method: UninstallMethod) {
        _uninstallMethod.value = method
        SpUtils.setParam(getApplication(), "method", method.value)
    }

    /**
     * 使用 Shizuku 异步执行给定的终端命令。
     * @param command 要执行的命令字符串。
     */
    fun executeTerminalCommand(command: String) {
        viewModelScope.launch {
            _terminalResult.value = OShizuku.exec(command.toByteArray())
        }
    }

    /**
     * 重置终端命令的执行结果，通常在UI上显示完结果后调用。
     */
    fun resetTerminalResult() {
        _terminalResult.value = null
    }

    /**
     * 根据当前选择的卸载方法卸载指定的应用。
     * @param appInfo 要卸载的应用信息对象。
     */
    fun uninstallApp(appInfo: AppInfo) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.PM_ENHANCED -> "pm uninstall --user 0 ${appInfo.appPkg}"
                UninstallMethod.PM -> "pm uninstall ${appInfo.appPkg}"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 ${appInfo.appPkg} i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 ${appInfo.appPkg} i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_U -> "service call package 132 s16 ${appInfo.appPkg} i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_V -> "service call package 133 s16 ${appInfo.appPkg} i32 0 i32 0"
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("卸载结果", result)
                refreshAppList()
            }
        }
    }

    /**
     * 重新安装（恢复）指定的应用。
     * @param appInfo 要重装的应用信息对象。
     */
    fun reinstallApp(appInfo: AppInfo) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 ${appInfo.appPkg} i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 ${appInfo.appPkg} i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_U -> "service call package 132 s16 ${appInfo.appPkg} i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_V -> "service call package 133 s16 ${appInfo.appPkg} i32 1 i32 0"
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

    /**
     * 根据包名卸载应用。
     * @param packageName 要卸载的应用包名。
     */
    fun uninstallPackageByName(packageName: String) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.PM_ENHANCED -> "pm uninstall --user 0 $packageName"
                UninstallMethod.PM -> "pm uninstall $packageName"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 $packageName i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 $packageName i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_U -> "service call package 132 s16 $packageName i32 0 i32 0"
                UninstallMethod.SERVICE_CALL_V -> "service call package 133 s16 $packageName i32 0 i32 0"
            }
            val result = OShizuku.exec(command.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("卸载结果", result)
                refreshAppList()
            }
        }
    }

    /**
     * 根据包名重新安装（恢复）应用。
     * @param packageName 要重装的应用包名。
     */
    fun reinstallPackageByName(packageName: String) {
        viewModelScope.launch {
            val command = when (_uninstallMethod.value) {
                UninstallMethod.SERVICE_CALL_T -> "service call package 131 s16 $packageName i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_S -> "service call package 134 s16 $packageName i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_U -> "service call package 132 s16 $packageName i32 1 i32 0"
                UninstallMethod.SERVICE_CALL_V -> "service call package 133 s16 $packageName i32 1 i32 0"
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

    /**
     * 显示一个对话框来展示 Shizuku 的操作结果。
     * @param title 对话框的标题。
     * @param result Shizuku 的执行结果。
     */
    private fun showResultDialog(title: String, result: ShizukuResult) {
        val message = result.output.ifBlank { "操作完成，无返回信息。" }
        val finalTitle = if (result.exitCode != 0) "$title (错误码: ${result.exitCode})" else title

        MessageDialog.build()
            .setTitle(finalTitle)
            .setMessage(message)
            .setOkButton("好") { _, _ ->
                refreshAppList()
                return@setOkButton false
            }
            .show()
    }

    /**
     * 从 `res/raw` 目录中动态加载应用列表。
     * 它会根据设备品牌自动选择合适的 `pkglist` 和 `optlist` 文件。
     * 例如，在小米设备上，它会尝试加载 `pkglist_hyper` 和 `optlist_hyper`。
     * 如果特定品牌的文件不存在，则会回退到默认的 `pkglist` 和 `optlist` 文件。
     */
    private fun loadAppLists() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val allPkgLists = mutableMapOf<String, List<AppInfo>>()
            val allOptLists = mutableMapOf<String, List<AppInfo>>()

            val rawFields = R.raw::class.java.fields
            for (field in rawFields) {
                try {
                    val resName = field.name
                    val resId = field.getInt(null)

                    val (listType, variant) = when {
                        resName.startsWith("pkglist") -> "pkglist" to (if (resName == "pkglist") "default" else resName.substringAfter("pkglist_"))
                        resName.startsWith("optlist") -> "optlist" to (if (resName == "optlist") "default" else resName.substringAfter("optlist_"))
                        else -> null to null
                    }

                    if (listType != null && variant != null) {
                        val appInfoList = context.resources.openRawResource(resId).use { inputStream ->
                            BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                                lines.filter { it.isNotBlank() }.map { AppInfo(appName = "", appPkg = it.trim()) }.toList()
                            }
                        }
                        if (listType == "pkglist") {
                            allPkgLists[variant] = appInfoList
                        } else {
                            allOptLists[variant] = appInfoList
                        }
                    }
                } catch (e: Exception) {
                    OLog.e("Error loading raw resource", e)
                }
            }

            val brand = Build.BRAND.lowercase()
            val variant = when (brand) {
                "xiaomi", "redmi", "poco" -> "hyper"
                "vivo","iqoo" -> "vivo"
                "oneplus","oppo" -> "color"
                "meizu" -> "meizu"
                "samsung" -> "samsung"
                else -> brand
            }

            _pkglist.value = allPkgLists[variant] ?: allPkgLists["default"] ?: emptyList()
            _optlist.value = allOptLists[variant] ?: allOptLists["default"] ?: emptyList()

            // After loading base lists, generate full details
            generateAppListDetails()
        }
    }

    /**
     * 刷新应用列表，重新加载应用详情。
     */
    fun refreshAppList() {
        viewModelScope.launch {
            _isRefreshing.value = true
            generateAppListDetails()
            _isRefreshing.value = false
        }
    }

    /**
     * 为 `pkglist` 和 `optlist` 中的每个应用生成详细信息，
     * 包括应用名称和图标。
     */
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

    /**
     * 更新单个应用的信息。
     * @param appInfo 要更新的应用信息。
     * @param context 应用上下文。
     * @param packageManager 包管理器实例。
     * @return 更新后的 AppInfo 对象。
     */
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

    /**
     * 根据包名获取应用名称。
     * @param context 应用上下文。
     * @param packageName 应用的包名。
     * @return 应用的名称，如果未安装则返回 "未安装"。
     */
    private fun getAppNameByPackageName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val applicationInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            "未安装"
        }
    }

    /**
     * 检查应用是否被禁用。
     * @param appPackageName 应用的包名。
     * @param packageManager 包管理器实例。
     * @return 如果应用被禁用则返回 `true`，否则返回 `false`。
     */
    private fun isAppDisabled(appPackageName: String, packageManager: PackageManager): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(appPackageName, 0)
            packageInfo.applicationInfo?.let { !it.enabled } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 修改系统设置以关闭后台缓存进程和虚进程的数量限制。
     * 这是一个高风险操作，可能导致设备不稳定。
     */
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

    /**
     * 还原对后台进程限制的修改。
     */
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

    /**
     * 通过将 "hd" 添加到状态栏图标黑名单中来隐藏 VoLTE HD 图标。
     */
    fun hideHD() {
        viewModelScope.launch {
            OLog.i("隐藏HD", "Shizuku方案")
            val result = OShizuku.exec("settings get secure icon_blacklist".toByteArray())
            val currentBlacklist = result.output.trim()
            OLog.i("隐藏HD", "当前黑名单列表: $currentBlacklist")

            val iconSet = currentBlacklist.split(',')
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableSet()

            iconSet.add("hd")
            iconSet.add("rotate") // 根据原始逻辑，同时添加 rotate

            val newBlacklist = iconSet.joinToString(",")
            OLog.i("隐藏HD", "处理后黑名单列表: $newBlacklist")
            OShizuku.exec("settings put secure icon_blacklist \"$newBlacklist\"".toByteArray())
        }
    }

    /**
     * 从状态栏图标黑名单中移除 "hd"，以恢复 VoLTE HD 图标的显示。
     */
    fun unhideHD() {
        viewModelScope.launch {
            OLog.i("还原HD", "Shizuku方案")
            val result = OShizuku.exec("settings get secure icon_blacklist".toByteArray())
            val currentBlacklist = result.output.trim()
            OLog.i("还原HD", "待处理数据: $currentBlacklist")

            val iconSet = currentBlacklist.split(',')
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toMutableSet()

            iconSet.remove("hd")

            val newBlacklist = iconSet.joinToString(",")
            OLog.i("还原HD", "处理结果: $newBlacklist")

            OShizuku.exec("settings put secure icon_blacklist \"$newBlacklist\"".toByteArray())
        }
    }

    /**
     * 应用 `OData.configdata.shell` 中定义的系统参数优化。
     */
    fun settingsOpt() {
        viewModelScope.launch {
            val result = OShizuku.exec(OData.configdata.shell.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("系统参数调优", result)
            }
        }
    }

    /**
     * 恢复 `OData.configdata.restore` 中定义的系统参数。
     */
    fun settingsRestore() {
        viewModelScope.launch {
            val result = OShizuku.exec(OData.configdata.restore.toByteArray())
            withContext(Dispatchers.Main) {
                showResultDialog("还原系统参数", result)
            }
        }
    }
} 