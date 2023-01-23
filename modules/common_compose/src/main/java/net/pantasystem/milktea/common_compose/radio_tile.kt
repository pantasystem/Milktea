package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RadioTile(selected: Boolean, onClick: () -> Unit, title: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick.invoke() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)

        Box(modifier = Modifier.weight(1f)) {
            title.invoke(this@Row)
        }

    }
}

@Preview
@Composable
fun PreviewRadioTile() {
    Column {
        RadioTile(selected = true, onClick = { /*TODO*/ }) {
            Text("はるのん")
        }
        RadioTile(selected = false, onClick = { /*TODO*/ }) {
            Text("ぬるきゃ")
        }
        RadioTile(selected = false, onClick = { /*TODO*/ }) {
            Text("あずきゃ")
        }
        RadioTile(selected = false, onClick = { /*TODO*/ }) {
            Text("村上さん")
        }
    }

}