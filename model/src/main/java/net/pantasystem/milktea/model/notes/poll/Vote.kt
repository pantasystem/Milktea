package net.pantasystem.milktea.model.notes.poll

data class Vote(
    val i: String,
    val choice: Int,
    val noteId: String
)