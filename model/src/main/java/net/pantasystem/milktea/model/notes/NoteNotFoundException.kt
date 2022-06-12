package net.pantasystem.milktea.model.notes

open class NoteNotFoundException(noteId: Note.Id, msg: String = "ノートを見つけることができませんでした: $noteId") : Exception(msg)