package net.pantasystem.milktea.model.notes

class NoteDeletedException(noteId: Note.Id) : NoteNotFoundException(noteId)