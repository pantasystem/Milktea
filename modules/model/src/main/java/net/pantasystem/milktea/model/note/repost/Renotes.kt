package net.pantasystem.milktea.model.note.repost

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.User

sealed interface RenoteType {
    data class Renote(
        val noteId: Note.Id,
        val isQuote: Boolean
    ) : RenoteType

    data class Reblog(
        val userId: User.Id
    ) : RenoteType
}
