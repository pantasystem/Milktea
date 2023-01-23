package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.model.notes.Note


data class ReactionHistoryRequest(
    val noteId: Note.Id,
    val type: String?,
)