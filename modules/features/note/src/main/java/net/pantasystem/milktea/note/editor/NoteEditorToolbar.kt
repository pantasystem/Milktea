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
    val title = when (visibility) {
        is Visibility.Followers -> stringResource(id = R.string.visibility_follower)
        is Visibility.Home -> stringResource(id = R.string.visibility_home)
        is Visibility.Public -> stringResource(id = R.string.visibility_public)
        is Visibility.Specified -> stringResource(id = R.string.visibility_specified)
        is Visibility.Limited -> stringResource(id = R.string.visibility_limited)
        Visibility.Mutual -> stringResource(id = R.string.visibility_mutual)
        Visibility.Personal -> stringResource(id = R.string.visibility_personal)
    }

    val iconDrawable = when (visibility) {
        is Visibility.Followers -> R.drawable.ic_lock_black_24dp
        is Visibility.Home -> R.drawable.ic_home_black_24dp
        is Visibility.Public -> R.drawable.ic_language_black_24dp
        is Visibility.Specified -> R.drawable.ic_email_black_24dp
        is Visibility.Limited -> net.pantasystem.milktea.common_android.R.drawable.ic_groups
        Visibility.Mutual -> net.pantasystem.milktea.common_android.R.drawable.ic_sync_alt_24px
        Visibility.Personal -> net.pantasystem.milktea.common_android.R.drawable.ic_person_black_24dp
    }

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
            IconButton(onClick = onNavigateUpButtonClicked, Modifier.size(40.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            AvatarIcon(
                url = currentUser?.avatarUrl,
                onAvatarClick = onAvatarIconClicked,
                borderStrokeWidth = 1.dp,
                borderStrokeColor = Color.Gray,
                size = 40.dp
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
                Modifier.size(40.dp),
            ) {
                Icon(painterResource(id = iconDrawable), contentDescription = title)
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onScheduleButtonClicked,
                Modifier.size(40.dp)
            ) {
                // schedule icon
                Icon(
                    painterResource(id = R.drawable.ic_baseline_edit_calendar_24),
                    contentDescription = null
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