package net.pantasystem.milktea.model.notes

class NoteDeletedException(noteId: Note.Id) : NoteNotFoundException(noteId)
class NoteRemovedException(noteId: Note.Id) : NoteNotFoundException(noteId)