package net.pantasystem.milktea.note.clip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.CircleCheckbox
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.clip.ClipId
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

@Composable
fun ToggleAddNoteToClipTile(
    modifier: Modifier = Modifier,
    noteId: Note.Id,
    clip: Clip,
    isAdded: Boolean,
    onClick: (Note.Id, Clip) -> Unit
) {
    Surface(
        modifier
            .fillMaxWidth()

            .clickable { onClick(noteId, clip) },
        color = MaterialTheme.colors.surface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(clip.name, modifier = Modifier.weight(1f))
            CircleCheckbox(selected = isAdded)
        }
    }


}

@Preview
@Composable
fun Preview_ToggleAddNoteToClipTile() {
    ToggleAddNoteToClipTile(
        noteId = Note.Id(0L, ""),
        clip = Clip(
            id = ClipId(accountId = 0, clipId = ""),
            createdAt = Clock.System.now(),
            userId = User.Id(accountId = 0, id = ""),
            name = "なんかいろいろ追加した",
            description = null,
            isPublic = false
        ),
        isAdded = true,
        onClick = { _, _ ->

        }
    )
}