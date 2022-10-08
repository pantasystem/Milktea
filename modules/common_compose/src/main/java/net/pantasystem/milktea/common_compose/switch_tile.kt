package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SwitchTile(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
    label: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.clickable { 
            onChanged.invoke(!checked)
        },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            label.invoke(this@Row)
        }

        Switch(checked = checked, onCheckedChange = onChanged, enabled = enabled)
    }
}