package net.pantasystem.milktea.setting.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingTitleTile(text: String) {
    Text(
        text = text,
        fontSize = 24.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
    )
}