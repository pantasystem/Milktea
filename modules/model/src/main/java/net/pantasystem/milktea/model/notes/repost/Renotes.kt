package net.pantasystem.milktea.model.notes.repost

import net.pantasystem.milktea.model.notes.Note
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
