package net.pantasystem.milktea.model.note

open class NoteNotFoundException(noteId: Note.Id, msg: String = "ノートを見つけることができませんでした: $noteId") : Exception(msg)