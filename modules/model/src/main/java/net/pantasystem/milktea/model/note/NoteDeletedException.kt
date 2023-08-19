package net.pantasystem.milktea.model.note

class NoteDeletedException(noteId: Note.Id) : NoteNotFoundException(noteId)
