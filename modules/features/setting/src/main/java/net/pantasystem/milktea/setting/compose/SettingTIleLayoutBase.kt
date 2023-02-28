package net.pantasystem.milktea.setting.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingListTileLayout(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = 12.dp,
    horizontalPadding: Dp = 14.dp,
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier.padding(vertical = verticalPadding, horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke(this)
        if (leading != null) {
            Spacer(modifier = Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            CompositionLocalProvider(
                LocalTextStyle provides TextStyle(
                    fontSize = 18.sp,
                )
            ) {
                title.invoke(this@Column)
            }

            CompositionLocalProvider(LocalTextStyle provides TextStyle(fontSize = 14.sp)) {
                subtitle?.invoke(this@Column)
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(4.dp))
        }
        trailing?.invoke(this)
    }
}

@Composable
fun SettingListTileLayout(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    verticalPadding: Dp = 8.dp,
    horizontalPadding: Dp = 14.dp,
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    SettingListTileLayout(
        modifier = modifier.clickable {
            onClick()
        },
        verticalPadding = verticalPadding,
        horizontalPadding = horizontalPadding,
        subtitle = subtitle,
        leading = leading,
        trailing = trailing,
        title = title,
    )
}

@Preview
@Composable
fun Preview_SettingTileLayout() {
    SettingListTileLayout(
        onClick = {},
        trailing = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        leading = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        subtitle = {
            Text("hogehogehogehogheo")
        }
    ) {
        Text("testtest")
    }
}