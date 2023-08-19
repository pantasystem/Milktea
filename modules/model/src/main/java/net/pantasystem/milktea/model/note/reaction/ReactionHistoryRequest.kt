package net.pantasystem.milktea.model.note.reaction

import net.pantasystem.milktea.model.note.Note


data class ReactionHistoryRequest(
    val noteId: Note.Id,
    val type: String?,
)