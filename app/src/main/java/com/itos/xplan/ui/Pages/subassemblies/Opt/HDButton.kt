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
import androidx.compose.ui.unit.dp
import com.itos.xplan.ui.viewmodel.AppViewModel

@Composable
fun HDButton(viewModel: AppViewModel) {
    Row {
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { viewModel.hideHD() }
        ) {
            Text("隐藏HD")
        }
        Spacer(modifier = Modifier.width(25.dp))
        FilledTonalButton(
            modifier = Modifier
                .size(width = 130.dp, height = 70.dp),
            shape = RoundedCornerShape(30),
            onClick = { viewModel.unhideHD() }
        ) {
            Text("还原HD")
        }
    }
}

