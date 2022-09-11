package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.model.notes.Note


data class CreateReaction(
    val noteId: Note.Id,
    val reaction: String
)