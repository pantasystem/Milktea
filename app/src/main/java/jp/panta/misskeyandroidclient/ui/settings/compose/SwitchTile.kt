package jp.panta.misskeyandroidclient.ui.settings.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SwitchTile(
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    label: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            label.invoke(this@Row)
        }

        Switch(checked = checked, onCheckedChange = onChanged)
    }
}