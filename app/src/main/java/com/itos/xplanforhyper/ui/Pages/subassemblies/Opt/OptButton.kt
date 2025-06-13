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
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.itos.xplanforhyper.XPlanForHyper
import com.itos.xplanforhyper.ui.viewmodel.AppViewModel
import com.itos.xplanforhyper.utils.OShizuku

@Composable
fun OptButton(activity: XPlanForHyper, viewModel: AppViewModel){
    Row(
        modifier = Modifier
            .padding(vertical = 45.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { opt_setappstauts(activity, viewModel, false) }
        ) {
            Text("一键优化")
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { opt_setappstauts(activity, viewModel, true) }
        ) {
            Text("还原")
        }
    }
}

fun opt_setappstauts(activity: XPlanForHyper, viewModel: AppViewModel, status: Boolean) {
    if (activity.b && activity.c) {
        viewModel.refreshAppList()
        // 遍历app list
        for (appInfo in viewModel.optlist) {
            if (appInfo.isExist) {
                activity.SetAppDisabled(appInfo.copy(isDisabled = status))
            }
        }
        if (!status) {
            MaterialAlertDialogBuilder(activity)
                .setTitle("完成")
                .setMessage("一键优化完成")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        } else {
            MaterialAlertDialogBuilder(activity)
                .setTitle("完成")
                .setMessage("还原完成")
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        viewModel.refreshAppList()
    } else {
        OShizuku.checkShizuku()
    }
}
