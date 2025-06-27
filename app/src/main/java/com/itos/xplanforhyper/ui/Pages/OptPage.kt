package com.itos.xplanforhyper.ui.Pages

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itos.xplanforhyper.XPlanForHyper
import com.itos.xplanforhyper.XPlanForHyper.Companion.app
import com.itos.xplanforhyper.ui.Pages.subassemblies.Opt.AutoBoostBotton
import com.itos.xplanforhyper.ui.Pages.subassemblies.Opt.ControlSystemUpdateButton
import com.itos.xplanforhyper.ui.Pages.subassemblies.Opt.HDButton
import com.itos.xplanforhyper.ui.Pages.subassemblies.Opt.OptButton
import com.itos.xplanforhyper.ui.Pages.subassemblies.Opt.ProcessLimitButton
import com.itos.xplanforhyper.ui.theme.OriginPlanTheme
import com.itos.xplanforhyper.ui.viewmodel.AppViewModel
import com.itos.xplanforhyper.utils.OData



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptPage(viewModel: AppViewModel?) {
    val activity = XPlanForHyper.app
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "优化")
                },
                actions = {
//                IconButton(
//                    onClick = {
////                        SettingsDebug()
//
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.Build,
//                        contentDescription = "Debug"
//                    )
//                }
                    IconButton(
                        onClick = {
                            MaterialAlertDialogBuilder(app)
                                .setTitle("公告")
                                .setMessage(app.show_notice)
                                .setPositiveButton("了解") { dialog, which ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "help"
                        )
                    }
                }
            )
        }) { innerPadding ->
        if (isLandscape(app)) {
            val scrollState = rememberScrollState()
            // 执行横屏时的操作
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
//                    .verticalScroll(scrollState)
//                    .padding(top = getStatusBarHeight().dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly // 将子项垂直居中
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    viewModel?.let {
                        OptButton(activity, it)
//                        ProcessLimitButton(it)
                        AutoBoostBotton()
                    }
                }

                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
                    viewModel?.let {
                        HDButton(it)
                        ControlSystemUpdateButton(it)
                    }
                }
            }
        } else {
            // 执行竖屏时的操作
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly // 将子项垂直居中
            ) {
                if (viewModel != null) {
                    OptButton(activity, viewModel)
                    AutoBoostBotton()
//                    ProcessLimitButton(it)
                    HDButton(viewModel)
                    ControlSystemUpdateButton(viewModel)
                }
            }
        }

    }
}

fun isLandscape(context: Context): Boolean {
    val configuration = context.resources.configuration
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun getStatusBarHeight(): Int {
    var result = 0
    val resourceId: Int = app.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = app.resources.getDimensionPixelSize(resourceId)
    }

    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun a() {
    OriginPlanTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            OptPage(null)
        }
    }
}
