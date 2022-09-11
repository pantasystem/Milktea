package net.pantasystem.milktea.model.notes.renote

import net.pantasystem.milktea.model.notes.Note

sealed interface Renote {
    val noteId: Note.Id
    data class Quote(
        override val noteId: Note.Id
    ) : Renote

    data class Normal(
        override val noteId: Note.Id
    ) : Renote
}
