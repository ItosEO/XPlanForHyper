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
import com.itos.xplan.utils.OShizuku

@Composable
fun AutoBoostBotton(){
    val activity = XPlanForHyper.app
    Row {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { boost(activity) }
        ) {
            Text("一键狂暴", textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { restore(activity) }
        ) {
            Text("还原操作", textAlign = TextAlign.Center)
        }
    }
}

fun boost (activity: XPlanForHyper){
    if (activity.b && activity.c) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("一键狂暴")
            .setMessage("该操作可能导致异常发热、您确定要进行吗？")
            .setPositiveButton("确定") { _, _ ->
                OShizuku.exec("pm clear com.miui.powerkeeper".toByteArray())
                OShizuku.exec("pm clear com.xiaomi.joyose".toByteArray())
                OShizuku.setAppDisabled("com.xiaomi.joyose",true)
                OShizuku.setAppDisabled("com.miui.powerkeeper", true)
                MaterialAlertDialogBuilder(activity)
                    .setTitle("完成")
                    .setMessage("操作完成")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
            .setNegativeButton("取消",null)
            .show()
    } else {
        OShizuku.checkShizuku()
    }
}
fun restore(activity: XPlanForHyper) {
    if (activity.b && activity.c) {
        OShizuku.setAppDisabled("com.xiaomi.joyose",false)
        OShizuku.setAppDisabled("com.miui.powerkeeper", false)
        MaterialAlertDialogBuilder(activity)
            .setTitle("完成")
            .setMessage("操作完成")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    } else {
        OShizuku.checkShizuku()
    }
}