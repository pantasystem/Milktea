package net.pantasystem.milktea.setting.compose

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingTileLayout(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    subtitle: (@Composable ColumnScope.() -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    title: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier.padding(vertical = 12.dp, horizontal = 14.dp),
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
                    fontWeight = FontWeight.Bold
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

@Preview
@Composable
fun Preview_SettingTileLayout() {
    SettingTileLayout(
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