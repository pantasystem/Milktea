package net.pantasystem.milktea.model.notes

/**
 * @param ancestors 親投稿
 * @param descendants 子投稿
 */
data class NoteThreadContext(
    val ancestors: List<Note>,
    val descendants: List<Note>,
)