package net.pantasystem.milktea.setting.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingRadioTile(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    SettingListTileLayout(
        modifier = modifier,
        leading = leading,
        onClick = onClick,
        subtitle = subtitle,
        title = title,
        trailing = {

            RadioButton(selected = selected, onClick = onClick)
        }
    )
}

@Preview
@Composable
fun Preview_SettingRadioTile() {
    SettingRadioTile(selected = true, onClick = {}) {
        Text("testtest")
    }
}