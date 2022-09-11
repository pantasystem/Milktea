package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.Serializable

@Serializable data class CreateReactionDTO (val i: String, val noteId: String, val reaction: String)