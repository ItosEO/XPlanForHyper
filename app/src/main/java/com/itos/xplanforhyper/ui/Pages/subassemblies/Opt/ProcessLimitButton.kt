package com.itos.xplanforhyper.ui.Pages.subassemblies.Opt

import android.annotation.SuppressLint
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
import com.itos.xplanforhyper.ui.viewmodel.AppViewModel

@SuppressLint("StaticFieldLeak")

@Composable
fun ProcessLimitButton(viewModel: AppViewModel){
    Row(
        modifier = Modifier
            .padding(vertical = 45.dp)
    ) {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = {
                viewModel.patchProcessLimit()
            }
        ) {
            Text("调整后台进程设置", textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = {
                viewModel.unpatchProcessLimit()
            }
        ) {
            Text("还原\n进程设置", textAlign = TextAlign.Center)
        }
    }
}