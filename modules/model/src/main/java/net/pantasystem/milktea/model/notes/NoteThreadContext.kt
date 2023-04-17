package net.pantasystem.milktea.model.notes

data class NoteThreadContext(
    val ancestors: List<Note>,
    val descendants: List<Note>,
)