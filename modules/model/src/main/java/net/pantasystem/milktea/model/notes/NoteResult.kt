package net.pantasystem.milktea.model.notes

sealed interface NoteResult {
    data class Success(val note: Note) : NoteResult
    object NotFound : NoteResult
    object Deleted : NoteResult
}