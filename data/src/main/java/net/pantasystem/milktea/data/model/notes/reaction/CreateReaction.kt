package net.pantasystem.milktea.data.model.notes.reaction

import net.pantasystem.milktea.data.model.notes.Note


data class CreateReaction(
    val noteId: Note.Id,
    val reaction: String
)