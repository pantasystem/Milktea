package net.pantasystem.milktea.model.note.reaction

import net.pantasystem.milktea.model.note.Note


data class CreateReaction(
    val noteId: Note.Id,
    val reaction: String
)