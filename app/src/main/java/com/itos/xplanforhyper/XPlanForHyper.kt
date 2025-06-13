package com.itos.xplanforhyper

import AboutPage
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alibaba.fastjson.JSONObject
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.itos.xplanforhyper.datatype.AppInfo
import com.itos.xplanforhyper.datatype.UninstallMethod
import com.itos.xplanforhyper.ui.Pages.OptPage
import com.itos.xplanforhyper.ui.theme.OriginPlanTheme
import com.itos.xplanforhyper.ui.viewmodel.AppViewModel
import com.itos.xplanforhyper.utils.NetUtils
import com.itos.xplanforhyper.utils.OData
import com.itos.xplanforhyper.utils.OLog
import com.itos.xplanforhyper.utils.OShizuku
import com.itos.xplanforhyper.utils.OShizuku.checkShizuku
import com.itos.xplanforhyper.utils.OUI
import com.itos.xplanforhyper.utils.SpUtils
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.interfaces.BaseDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.NoTouchInterface
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnBinderReceivedListener

// TODO 拆Details页面

class XPlanForHyper : AppCompatActivity() {
    val context: Context = this
    var b = true
    var c = false
    var show_notice: String = "暂无公告"
    
    private val viewModel: AppViewModel by viewModels()

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _: Int, _: Int ->
            this.onRequestPermissionsResult()
        }
    private val BINDER_RECEVIED_LISTENER =
        OnBinderReceivedListener {
            checkShizuku()
        }
    private val BINDER_DEAD_LISTENER: Shizuku.OnBinderDeadListener =
        Shizuku.OnBinderDeadListener {
            checkShizuku()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OriginPlanTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppListContent(viewModel)
                }
            }
        }
        app = this
        DialogX.init(this)
        DialogX.onlyOnePopTip = true
        DialogX.globalTheme = DialogX.THEME.AUTO

        //设置为MaterialYou主题
        DialogX.globalStyle = MaterialYouStyle()
        DialogX.dialogLifeCycleListener = object : DialogLifecycleCallback<BaseDialog>() {
            override fun onShow(dialog: BaseDialog) {
                super.onShow(dialog)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dialog !is NoTouchInterface) {
                    val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                    (dialog.ownActivity.window.decorView as ViewGroup).getChildAt(0)
                        .setRenderEffect(blurEffect)
                }
            }

            override fun onDismiss(dialog: BaseDialog) {
                super.onDismiss(dialog)
                val sameActivityRunningDialog = BaseDialog.getRunningDialogList(dialog.ownActivity)
                val iterator = sameActivityRunningDialog.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next() is NoTouchInterface) {
                        iterator.remove()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (dialog !is PopTip) && (dialog !is PopNotification) && (sameActivityRunningDialog.isEmpty() || sameActivityRunningDialog[0] === dialog)) {
                    (dialog.ownActivity.window.decorView as ViewGroup).getChildAt(0)
                        .setRenderEffect(null)
                }
            }
        }
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        
        checkShizuku()
        Shizuku.addBinderReceivedListenerSticky(BINDER_RECEVIED_LISTENER)
        Shizuku.addBinderDeadListener(BINDER_DEAD_LISTENER)
        guide()
        update_notice()
    }

    private fun guide() {
        if (SpUtils.getParam(context, "if_first_time", true) as Boolean) {
            MaterialAlertDialogBuilder(context)
                .setTitle("帮助")
                .setMessage("您需要Shiuzku激活教程吗")
                .setPositiveButton("好的") { _, _ ->
                    SpUtils.setParam(context, "if_first_time", false)
                    OUI.openLink("https://www.bilibili.com/video/BV1o94y1u7Kq")
                }
                .setNegativeButton("我会") { dialog, _ ->
                    SpUtils.setParam(context, "if_first_time", false)
                    dialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }
    }

    private fun update_notice() {
        val handler = CoroutineExceptionHandler { _, exception ->
            // 在这里处理异常，例如打印日志、上报异常等
            OLog.e("Update Notice Exception:", exception)
            MaterialAlertDialogBuilder(context)
                .setTitle("错误")
                .setMessage("连接服务器失败\n请检查网络连接")
                .setPositiveButton("了解", null)
                .show()
        }
        lifecycleScope.launch(Dispatchers.IO + handler) {
            // 后台工作
            val update = NetUtils.Get(OData.updataUrl)
            // 切换到主线程进行 UI 操作
            withContext(Dispatchers.Main) {
                // UI 操作，例如显示 Toast
                val jsonObject = JSONObject.parseObject(update)
                val version = jsonObject.getString("version")
                val url = jsonObject.getString("url")
                val version_name = jsonObject.getString("version_name")
                val log = jsonObject.getString("log")
                val isShowNotice = jsonObject.getBoolean("isShowNotice")
                val notice = jsonObject.getString("notice")
                show_notice = notice
                OLog.i(
                    "更新",
                    update + "\n" + version + "\n" + url + "\n" + version_name + "\n" + log + "\n" + isShowNotice + "\n" + notice
                )
                if (BuildConfig.VERSION_CODE < version.toInt()) {
                    OLog.i("更新", "有新版本")
                    MaterialAlertDialogBuilder(context)
                        .setTitle("有新版本")
                        .setMessage("最新版本：$version_name($version)\n\n更新日志：\n$log")
                        .setPositiveButton("前往更新") { _, _ ->
                            OUI.openLink(url)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    if (isShowNotice) {
                        OLog.i("公告", "显示")
                        MaterialAlertDialogBuilder(context)
                            .setTitle("公告")
                            .setMessage(notice)
                            .setPositiveButton("我知道了") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }

            }
        }
    }


    private fun onRequestPermissionsResult() {
        checkShizuku()
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeBinderReceivedListener(BINDER_RECEVIED_LISTENER)
        Shizuku.removeBinderDeadListener(BINDER_DEAD_LISTENER)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    fun SetAppDisabled(
        appInfo: AppInfo
    ): Boolean? {
        if (appInfo.isExist) {
            OShizuku.setAppDisabled(appInfo.appPkg, !appInfo.isDisabled)
            // We must refresh the state from the source of truth
            viewModel.refreshAppList()
            // To provide immediate feedback, we can check, but it's not the compose way
            val c = isAppDisabled(appInfo.appPkg)
            if (c != appInfo.isDisabled) {
                return true
            } else {
                Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            Toast.makeText(this, "应用未安装", Toast.LENGTH_SHORT).show()
            return null
        }
    }


    private fun getAppNameByPackageName(context: Context, packageName: String): String {
        val packageManager: PackageManager = context.packageManager
        val applicationInfo: ApplicationInfo? = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        return applicationInfo?.let {
            packageManager.getApplicationLabel(it).toString()
        } ?: "未安装"
    }


    /****************
     *
     * 发起添加群流程。群号：IQOO⭐️交流群(262040855) 的 key 为： SqLJvDGqjKNDvc_O5dx6A164eLSo4QBG
     * 调用 joinQQGroup(SqLJvDGqjKNDvc_O5dx6A164eLSo4QBG) 即可发起手Q客户端申请加群 IQOO⭐️交流群(262040855)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    private fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: java.lang.Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    private fun join_qq() {
        val is_join_succeed = joinQQGroup("SqLJvDGqjKNDvc_O5dx6A164eLSo4QBG")
        if (!is_join_succeed) {
            Toast.makeText(
                this,
                "未安装手Q或安装的版本不支持, 请手动加群262040855",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun isAppDisabled(appPackageName: String): Boolean {
        val packageManager: PackageManager = context.packageManager

        return try {
            val packageInfo = packageManager.getPackageInfo(appPackageName, 0)
            // 应用被停用或者处于默认状态（未设置启用状态），返回 true；其他状态返回 false
            !packageInfo.applicationInfo?.enabled!!
        } catch (e: Exception) {
            false
        }
    }


    @Composable
    fun AppListItem(
        appInfo: AppInfo,
        onSetDisabled: (AppInfo) -> Unit,
        onUninstall: (AppInfo) -> Unit,
        onReinstall: (AppInfo) -> Unit
    ) {
        var isMenuVisible by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            OLog.i("重绘", "触发重绘")

            if (appInfo.appIcon != null) {
                Image(
                    painter = rememberDrawablePainter(appInfo.appIcon),
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically),
                    contentDescription = null
                )
            }
            // 左边显示应用名称
            Column(modifier = Modifier.weight(0.5f)) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (!appInfo.isExist) Color(0xFFFF6E40) else LocalContentColor.current
                )
                Text(text = appInfo.appPkg, style = MaterialTheme.typography.bodySmall)
            }

            // 中间显示禁用状态文本
            Text(
                text = if (!appInfo.isExist) "Unknown" else if (appInfo.isDisabled) "Disable" else "Enable",
                color = if (!appInfo.isExist) Color(0xFFFF6E40)
                else if (appInfo.isDisabled) Color(0xFFFF5252)
                else Color(0xFF59F0A6),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            // 右边是一个按钮
            IconButton(
                onClick = {
                    onSetDisabled(appInfo)
                }
            ) {

                val icon: ImageVector = if (appInfo.isExist && appInfo.isDisabled) {
                    Icons.Default.Check
                } else if (appInfo.isExist) {
                    Icons.Default.Close
                } else {
                    Icons.Default.Warning
                }
                Icon(
                    imageVector = icon,

                    contentDescription = if (!appInfo.isExist) "Unknown" else if (appInfo.isDisabled) "Disable" else "Enable"
                )
            }
            IconButton(
                onClick = { isMenuVisible = true }
            ) {

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
                DropdownMenu(
                    expanded = isMenuVisible,
                    onDismissRequest = { isMenuVisible = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "uninstall"
                            )
                        },
                        text = { Text(text = "尝试卸载") },
                        onClick = {
                            isMenuVisible = false
                            onUninstall(appInfo)
                        }
                        // 处理菜单项点击事件，这里可以添加卸载逻辑
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "uninstall"
                            )
                        }, text = { Text(text = "尝试重装") }, onClick = {
                            isMenuVisible = false
                            onReinstall(appInfo)
                        })
                }
            }

        }

    }


    @Composable
    fun AppList(
        viewModel: AppViewModel
    ) {
        val appList = viewModel.pkglist
        val optList = viewModel.optlist

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(optList + appList) { appInfo ->
                AppListItem(
                    appInfo = appInfo,
                    onSetDisabled = { SetAppDisabled(it) },
                    onUninstall = { viewModel.uninstallApp(it) },
                    onReinstall = { viewModel.reinstallApp(it) }
                )
            }
        }
    }

    private fun copyText(text: String) = getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))

    private suspend fun onTerminalResult(exitValue: Int, msg: String?) =
        withContext(Dispatchers.Main) {
            if (exitValue == 0 && msg.isNullOrBlank()) return@withContext
            MaterialAlertDialogBuilder(context).apply {
                if (!msg.isNullOrBlank()) {
                    if (exitValue != 0) {
                        setTitle(getString(R.string.operation_failed, exitValue.toString()))
                    } else {
                        setTitle("结果")
                    }
                    setMessage(msg)
                    setNeutralButton(android.R.string.copy) { _, _ -> copyText(msg) }
                } else if (exitValue != 0) {
                    setMessage(getString(R.string.operation_failed, exitValue.toString()))
                }
            }.setPositiveButton(android.R.string.ok, null).show()
                .findViewById<MaterialTextView>(android.R.id.message)
                ?.setTextIsSelectable(true)
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Details(viewModel: AppViewModel) {
        var showDialog by remember { mutableStateOf(false) }
        val currentMethod by viewModel.uninstallMethod.collectAsState()
        val terminalResult by viewModel.terminalResult.collectAsState()

        LaunchedEffect(terminalResult) {
            terminalResult?.let { result ->
                onTerminalResult(result.exitCode, result.output)
                viewModel.resetTerminalResult() // Reset after showing
            }
        }

        if (showDialog) {
            val allMethods = UninstallMethod.entries.toTypedArray()
            var selectedMethod by remember { mutableStateOf(currentMethod) }
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("设置方案") },
                text = {
                    Column {
                        allMethods.forEach { method ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMethod = method }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (method == selectedMethod),
                                    onClick = { selectedMethod = method }
                                )
                                Text(
                                    text = method.displayName,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.setUninstallMethod(selectedMethod)
                            showDialog = false
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
        
        Column {
            TopAppBar(title = { Text(text = "XHyper") },
                actions = {
                    IconButton(onClick = {
                        val inputEditText = EditText(context)
                        inputEditText.hint = "Terminal"
                        inputEditText.inputType = InputType.TYPE_CLASS_TEXT

                        MaterialAlertDialogBuilder(context)
                            .setTitle("终端")
                            .setView(inputEditText)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.executeTerminalCommand(inputEditText.text.toString())
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()

                    }) {
                        Icon(
                            imageVector = ImageVector.Companion.vectorResource(R.drawable.ic_baseline_terminal),
                            contentDescription = "terminal"
                        )
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "settings"
                        )
                    }
                })
            AppList(viewModel = viewModel)
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppListScreen(viewModel: AppViewModel) {
        val navController = rememberNavController()
        Scaffold(
            //设置底部导航栏
            bottomBar = {
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    NavigationBarItem(

                        icon = {
                            when (currentDestination?.route) {
                                "1" -> {
                                    // 选中时的图标
                                    Icon(Icons.Filled.Settings, contentDescription = null)
                                }

                                else -> {
                                    // 未选中时的图标
                                    Icon(Icons.Outlined.Settings, contentDescription = null)
                                }
                            }
                        },
                        label = {
                            Text(
                                text = "Optimization"
//                                modifier = Modifier.alpha(if (currentDestination?.route == "Details") 1f else 0f)
                            )
                        },
                        selected = currentDestination?.route == "1",
                        alwaysShowLabel = false,
                        onClick = {
                            navController.navigate("1") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            when (currentDestination?.route) {
                                "2" -> {
                                    // 选中时的图标
                                    Icon(Icons.Filled.Create, contentDescription = null)
                                }

                                else -> {
                                    // 未选中时的图标
                                    Icon(Icons.Outlined.Create, contentDescription = null)
                                }
                            }
                        },
                        label = {
                            Text(
                                text = "Details",
//                                modifier = Modifier.alpha(if (currentDestination?.route == "Details") 1f else 0f)
                            )
                        },
                        selected = currentDestination?.route == "2",
                        alwaysShowLabel = false,
                        onClick = {
                            navController.navigate("2") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = {
                            when (currentDestination?.route) {
                                "3" -> {
                                    // 选中时的图标
                                    Icon(Icons.Filled.Info, contentDescription = null)
                                }

                                else -> {
                                    // 未选中时的图标
                                    Icon(Icons.Outlined.Info, contentDescription = null)
                                }
                            }
                        },
                        label = {
                            Text(
                                text = "About",
//                                modifier = Modifier.alpha(if (currentDestination?.route == "About") 1f else 0f)
                            )
                        },
                        selected = currentDestination?.route == "3",
                        alwaysShowLabel = false,
                        onClick = {
                            navController.navigate("3") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }


            }

        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = it.calculateBottomPadding()) // 添加 padding,防止遮挡内容
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "1"
                ) {
                    OLog.i("界面", "绘制横屏开始")
                    composable("2") { Details(viewModel) }
                    composable("3") { AboutPage() }
                    composable("1") { OptPage(viewModel) }
                    // 添加其他页面的 composable 函数，类似上面的示例
                }
            }
        }

    }


    @Composable
    fun AppListContent(viewModel: AppViewModel) {
        AppListScreen(viewModel)
    }

    @Preview(showBackground = true)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
    @Composable
    fun GreetingPreview() {
        OriginPlanTheme {
            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Text("XPlanForHyper Preview")
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var app: XPlanForHyper private set
    }
}



