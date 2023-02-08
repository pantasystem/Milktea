package net.pantasystem.milktea.note.clip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.CircleCheckbox
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipId
import net.pantasystem.milktea.model.user.User

@Composable
fun ToggleAddNoteToClipTile(
    modifier: Modifier = Modifier,
    clip: Clip,
    state: ClipAddState,
    onClick: () -> Unit
) {
    Surface(
        modifier
            .fillMaxWidth()

            .clickable { onClick() },
        color = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                clip.name,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                fontSize = 18.sp
            )

            when (state) {
                ClipAddState.Added -> CircleCheckbox(selected = true)
                ClipAddState.NotAdded -> CircleCheckbox(selected = false)
                ClipAddState.Progress -> CircularProgressIndicator()
                ClipAddState.Unknown -> Icon(
                    Icons.Default.QuestionMark,
                    contentDescription = "unknown added status"
                )
            }

        }
    }


}

@Preview
@Composable
fun Preview_ToggleAddNoteToClipTile() {
    ToggleAddNoteToClipTile(
        clip = Clip(
            id = ClipId(accountId = 0, clipId = ""),
            createdAt = Clock.System.now(),
            userId = User.Id(accountId = 0, id = ""),
            name = "なんかいろいろ追加した",
            description = null,
            isPublic = false
        ),
        state = ClipAddState.Added,
        onClick = {}
    )
}