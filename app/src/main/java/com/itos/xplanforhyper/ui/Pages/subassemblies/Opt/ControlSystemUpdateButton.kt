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
import com.itos.xplanforhyper.XPlanForHyper
import com.itos.xplanforhyper.ui.viewmodel.AppViewModel
import com.itos.xplanforhyper.utils.OShizuku
import com.itos.xplanforhyper.utils.SpUtils

@Composable
fun ControlSystemUpdateButton(viewModel: AppViewModel){
    val activity = XPlanForHyper.app
    Row(
        modifier = Modifier
            .padding(vertical = 45.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { disableSystemUpdate(activity, viewModel) }
        ) {
            Text("禁用\n系统更新", textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { enableSystemUpdate(activity, viewModel) }
        ) {
            Text("恢复\n系统更新", textAlign = TextAlign.Center)
        }
    }
}

private fun disableSystemUpdate(activity: XPlanForHyper, viewModel: AppViewModel){
    if (activity.b && activity.c) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("禁用系统更新")
            .setMessage("该操作可能导致卡米，您确定要进行吗？")
            .setPositiveButton("确定") { _, _ ->
                uninstall(activity, "com.android.updater")
                MaterialAlertDialogBuilder(activity)
                    .setTitle("完成")
                    .setMessage("禁用系统更新完成")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                viewModel.refreshAppList()
            }
            .setNegativeButton("取消",null)
            .show()
    } else {
        OShizuku.checkShizuku()
    }
}

private fun enableSystemUpdate(activity: XPlanForHyper, viewModel: AppViewModel){
    if (activity.b && activity.c) {
        reinstall(activity, "com.android.updater")
        MaterialAlertDialogBuilder(activity)
            .setTitle("完成")
            .setMessage("已恢复系统更新")
            .setPositiveButton(android.R.string.ok, null)
            .show()
        viewModel.refreshAppList()
    } else {
        OShizuku.checkShizuku()
    }
}

private fun uninstall(activity: XPlanForHyper, packagename: String) {
    when (SpUtils.getParam(activity, "method", 1)) {
        3 -> {
            activity.ShizukuExec("service call package 131 s16 $packagename i32 0 i32 0".toByteArray())
        }

        2 -> {
            activity.ShizukuExec("service call package 134 s16 $packagename i32 0 i32 0".toByteArray())
        }

        1 -> {
            activity.ShizukuExec("pm uninstall --user 0 $packagename".toByteArray())
        }
        else -> {
            activity.ShizukuExec("pm uninstall $packagename".toByteArray())
        }

    }
}

private fun reinstall(activity: XPlanForHyper, packagename: String) {
    when (SpUtils.getParam(activity, "method", 1)) {
        3 -> {
            activity.ShizukuExec("service call package 131 s16 $packagename i32 1 i32 0".toByteArray())
        }

        2 -> {
            activity.ShizukuExec("service call package 134 s16 $packagename i32 1 i32 0".toByteArray())
        }

        else -> {
            activity.ShizukuExec("pm install-existing $packagename".toByteArray())
        }
    }

}
