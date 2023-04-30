package net.pantasystem.milktea.model.notes.renote

import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

sealed interface Renote {
    data class Quote(
        val noteId: Note.Id
    ) : Renote

    data class Normal(
        val noteId: Note.Id
    ) : Renote

    data class Reblog(
        val userId: User.Id
    ) : Renote
}
