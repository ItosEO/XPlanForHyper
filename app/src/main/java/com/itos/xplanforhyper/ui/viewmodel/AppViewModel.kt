package com.itos.xplanforhyper.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itos.xplanforhyper.R
import com.itos.xplanforhyper.datatype.AppInfo
import com.itos.xplanforhyper.utils.OLog
import com.itos.xplanforhyper.utils.OPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _pkglist = mutableStateOf<List<AppInfo>>(emptyList())
    val pkglist: List<AppInfo> get() = _pkglist.value

    private val _optlist = mutableStateOf<List<AppInfo>>(emptyList())
    val optlist: List<AppInfo> get() = _optlist.value

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: Boolean get() = _isRefreshing.value

    init {
        loadAppLists()
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
} 