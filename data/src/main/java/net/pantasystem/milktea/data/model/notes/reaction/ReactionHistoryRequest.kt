package net.pantasystem.milktea.data.model.notes.reaction

import net.pantasystem.milktea.data.model.notes.Note


data class ReactionHistoryRequest(
    val noteId: Note.Id,
    val type: String?,
)