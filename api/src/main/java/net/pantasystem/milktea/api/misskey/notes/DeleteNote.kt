package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable data class DeleteNote(val i: String, val noteId: String)