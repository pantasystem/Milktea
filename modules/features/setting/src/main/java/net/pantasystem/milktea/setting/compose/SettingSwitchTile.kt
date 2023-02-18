package net.pantasystem.milktea.setting.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingSwitchTile(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onChanged: (Boolean) -> Unit,
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    SettingListTileLayout(
        modifier = modifier,
        leading = leading,
        onClick = {
            onChanged(!checked)
        },
        subtitle = subtitle,
        title = title,
        trailing = {
            Switch(checked = checked, onCheckedChange = onChanged)
        }
    )
}

@Preview
@Composable
fun Preview_SettingSwitchTile() {
    SettingSwitchTile(checked = true, onChanged = {}) {
        Text("testtest")
    }
}