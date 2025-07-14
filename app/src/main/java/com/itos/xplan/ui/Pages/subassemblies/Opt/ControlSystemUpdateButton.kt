package com.itos.xplan.ui.Pages.subassemblies.Opt

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.itos.xplan.XPlanForHyper
import com.itos.xplan.ui.viewmodel.AppViewModel
import com.itos.xplan.utils.OShizuku

@Composable
fun ControlSystemUpdateButton(viewModel: AppViewModel){
    val activity = XPlanForHyper.app
    Row {
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
                viewModel.uninstallPackageByName("com.android.updater")
                MaterialAlertDialogBuilder(activity)
                    .setTitle("完成")
                    .setMessage("禁用系统更新完成")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
            .setNegativeButton("取消",null)
            .show()
    } else {
        OShizuku.checkShizuku()
    }
}

private fun enableSystemUpdate(activity: XPlanForHyper, viewModel: AppViewModel){
    if (activity.b && activity.c) {
        viewModel.reinstallPackageByName("com.android.updater")
        MaterialAlertDialogBuilder(activity)
            .setTitle("完成")
            .setMessage("已恢复系统更新")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    } else {
        OShizuku.checkShizuku()
    }
}
