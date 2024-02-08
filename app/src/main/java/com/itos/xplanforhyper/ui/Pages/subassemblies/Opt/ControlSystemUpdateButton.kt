package com.itos.xplanforhyper.ui.Pages.subassemblies.Opt

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itos.xplanforhyper.XPlanForHyper.Companion.app
import com.itos.xplanforhyper.utils.OShizuku
import com.itos.xplanforhyper.utils.SpUtils

@Composable
fun ControlSystemUpdateButton(){
    Row(
        modifier = Modifier
            .padding(vertical = 45.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 60.dp),
            shape = RoundedCornerShape(30),
            onClick = { DisableSystemUpdate() }
        ) {
            Text("禁用\n系统更新", textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 60.dp),
            shape = RoundedCornerShape(30),
            onClick = { EnableSystemUpdate() }
        ) {
            Text("恢复\n系统更新", textAlign = TextAlign.Center)
        }
    }
}

private fun DisableSystemUpdate(){
    if (app.b && app.c) {
//        val isExist=OPackage.isInstalled("com.android.updater",XPlanForHyper.app.packageManager)
//        XPlanForHyper.app.SetAppDisabled(mutableStateOf(true), "com.android.updater", isExist, false)
        uninstall("com.android.updater")
        MaterialAlertDialogBuilder(app)
            .setTitle("完成")
            .setMessage("禁用系统更新完成")
            .setPositiveButton(android.R.string.ok, null)
            .show()
        app.generateAppList(app)
    } else {
        OShizuku.checkShizuku()
    }
}

private fun EnableSystemUpdate(){
    if (app.b && app.c) {
//        val isExist=OPackage.isInstalled("com.android.updater", app.packageManager)
//        app.SetAppDisabled(mutableStateOf(false), "com.android.updater", isExist, false)
        reinstall("com.android.updater")
        MaterialAlertDialogBuilder(app)
            .setTitle("完成")
            .setMessage("已恢复系统更新")
            .setPositiveButton(android.R.string.ok, null)
            .show()
        app.generateAppList(app)
    } else {
        OShizuku.checkShizuku()
    }
}

private fun uninstall(packagename: String) {
    when (SpUtils.getParam(app.context, "method", 1)) {
        3 -> {
            app.ShizukuExec("service call package 131 s16 $packagename i32 0 i32 0".toByteArray())
        }

        2 -> {
            app.ShizukuExec("service call package 134 s16 $packagename i32 0 i32 0".toByteArray())
        }

        1 -> {
            app.ShizukuExec("pm uninstall --user 0 $packagename".toByteArray())
        }
        else -> {
            app.ShizukuExec("pm uninstall $packagename".toByteArray())
        }

    }
}

private fun reinstall(packagename: String) {
    when (SpUtils.getParam(app, "method", 1)) {
        3 -> {
            app.ShizukuExec("service call package 131 s16 $packagename i32 1 i32 0".toByteArray())
        }

        2 -> {
            app.ShizukuExec("service call package 134 s16 $packagename i32 1 i32 0".toByteArray())
        }

        else -> {
            app.ShizukuExec("pm install-existing $packagename".toByteArray())
        }
    }

}
