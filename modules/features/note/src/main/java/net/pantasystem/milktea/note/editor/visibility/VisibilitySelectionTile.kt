package net.pantasystem.milktea.note.editor.visibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.note.R


@Composable
internal fun VisibilitySelectionTile(
    item: Visibility,
    isSelected: Boolean,
    onClick: (item: Visibility) -> Unit,
) {

    val title = stringVisibilityText(visibility = item)
    val iconDrawable = painterVisibilityIconResource(visibility = item)
    Surface(
        Modifier.clickable {
            onClick(item)
        },
        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(iconDrawable, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(title)
        }
    }
}


@Composable
internal fun VisibilityLocalOnlySwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable {
                onChanged.invoke(!checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.local_only))
        }

        Switch(checked = checked, onCheckedChange = onChanged, enabled = enabled)
    }
}
