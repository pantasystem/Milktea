package net.pantasystem.milktea.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SettingSection(
    title: String,
    modifier: Modifier = Modifier,
    paddingTop: Dp = 16.dp,
    paddingBottom: Dp = 0.dp,
    isNeedUnderDivider: Boolean = true,
    children: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        Column(
            modifier = modifier.padding(
                top = paddingTop,
                bottom = paddingBottom
            )
        ) {
            Text(
                title,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            children()

        }
        if (isNeedUnderDivider) {
            Divider(Modifier.fillMaxWidth())
        }
    }

}