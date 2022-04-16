package net.pantasystem.milktea.data.model.notes.poll

data class Vote(
    val i: String,
    val choice: Int,
    val noteId: String
)