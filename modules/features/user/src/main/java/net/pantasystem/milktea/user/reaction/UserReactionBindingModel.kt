package net.pantasystem.milktea.user.reaction

import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.reaction.UserReaction
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

class UserReactionBindingModel(
    val reaction: UserReaction,
    val note: PlaneNoteViewData,
    val user: StateFlow<User>,
) {
    val emojis
        get() = note.toShowNote.note.emojis

    val isCustomEmoji
        get() = Reaction(reaction.type).isCustomEmojiFormat()

    val isNotCustomEmojiFormat
        get() = !isCustomEmoji
}