package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.common_compose.AvatarIcon
import net.pantasystem.milktea.model.note.CanLocalOnly
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.visibility.painterVisibilityIconResource
import net.pantasystem.milktea.note.editor.visibility.stringVisibilityText

@Composable
fun NoteEditorToolbar(
    currentUser: User?,
    visibility: Visibility,
    textCount: Int = 0,
    validInputs: Boolean = false,
    onNavigateUpButtonClicked: () -> Unit,
    onAvatarIconClicked: () -> Unit,
    onVisibilityButtonClicked: () -> Unit,
    onScheduleButtonClicked: () -> Unit,
    onPostButtonClicked: () -> Unit,
) {
    val title = stringVisibilityText(visibility = visibility)

    val iconDrawable = painterVisibilityIconResource(visibility = visibility)
    val color = getColor(color = R.attr.normalIconTint)

    Row(
        Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUpButtonClicked) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    Modifier.size(24.dp),
                    tint = color,
                )
            }

            AvatarIcon(
                url = currentUser?.avatarUrl,
                onAvatarClick = onAvatarIconClicked,
                borderStrokeWidth = 1.dp,
                borderStrokeColor = Color.Gray,
                size = 30.dp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (visibility is CanLocalOnly && visibility.isLocalOnly) {
                Text(stringResource(id = R.string.local))
            }

            IconButton(
                onClick = onVisibilityButtonClicked,
            ) {
                Icon(
                    iconDrawable,
                    contentDescription = title,
                    Modifier.size(24.dp),
                    tint = color,
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onScheduleButtonClicked,
            ) {
                // schedule icon
                Icon(
                    painterResource(id = R.drawable.ic_baseline_edit_calendar_24),
                    contentDescription = null,
                    Modifier.size(24.dp),
                    tint = color,
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("$textCount")
            Spacer(modifier = Modifier.width(4.dp))
            Button(onClick = onPostButtonClicked, enabled = validInputs) {
                Text(stringResource(id = R.string.post))
            }
        }

    }
}

@Preview
@Composable
fun Preview_NoteEditorToolbar() {
    MaterialTheme {
        Surface {
            NoteEditorToolbar(
                currentUser = null,
                Visibility.Public(true),
                onAvatarIconClicked = {},
                onNavigateUpButtonClicked = {},
                onVisibilityButtonClicked = {},
                onScheduleButtonClicked = {},
                onPostButtonClicked = {},
            )
        }
    }
}