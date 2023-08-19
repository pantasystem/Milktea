package net.pantasystem.milktea.model.note.reaction

import net.pantasystem.milktea.model.note.Note

data class DeleteReaction(
    val noteId: Note.Id,
    val reaction: String,
)