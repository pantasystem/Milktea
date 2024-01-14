package net.pantasystem.milktea.model.note

sealed interface NoteResult {
    data class Success(val note: Note) : NoteResult
    data object NotFound : NoteResult
    data object Deleted : NoteResult
}