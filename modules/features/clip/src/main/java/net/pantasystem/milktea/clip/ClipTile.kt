package net.pantasystem.milktea.clip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common_compose.CircleCheckbox

@Composable
fun ClipTile(
    modifier: Modifier = Modifier,
    clipState: ClipItemState,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onAddToTabButtonClicked: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        color = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(clipState.clip.name, fontSize = 20.sp)
                if (clipState.clip.description != null) {
                    Text(clipState.clip.description ?: "", fontSize = 16.sp)
                }
            }

            if (isSelectMode) {
                CircleCheckbox(selected = isSelected)
            } else {
                AddToTabButton(isPaged = clipState.isAddedToTab, onPressed = onAddToTabButtonClicked)
            }

        }
    }
}


@Composable
@Stable
private fun AddToTabButton(isPaged: Boolean, onPressed: () -> Unit) {
    IconButton(onClick = onPressed) {
        if (isPaged) {
            Icon(
                imageVector = Icons.Default.BookmarkRemove,
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        } else {
            Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        }

    }
}