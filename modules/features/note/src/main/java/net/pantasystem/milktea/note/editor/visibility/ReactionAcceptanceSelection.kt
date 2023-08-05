package net.pantasystem.milktea.note.editor.visibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.model.notes.ReactionAcceptanceType
import net.pantasystem.milktea.note.R

@Composable
fun ReactionAcceptanceSelection(
    type: ReactionAcceptanceType?,
    isSelected: Boolean,
    onSelected: (ReactionAcceptanceType?) -> Unit,
) {
    val title = remember(type) {
        when(type) {
            ReactionAcceptanceType.LikeOnly -> R.string.reaction_acceptance_only_likes
            ReactionAcceptanceType.LikeOnly4Remote -> R.string.reaction_acceptance_like_only_for_remote
            ReactionAcceptanceType.NonSensitiveOnly -> R.string.reaction_acceptance_non_sensitive_only
            ReactionAcceptanceType.NonSensitiveOnly4LocalOnly4Remote -> R.string.reaction_acceptance_non_sensitive_only_likes_from_remote
            null -> R.string.reaction_acceptance_all
        }
    }
    Surface(
        Modifier.clickable {
            onSelected(type)
        },
        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(id = title))
        }
    }
}