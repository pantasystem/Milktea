package net.pantasystem.milktea.api.misskey.notes.favorite

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFavorite (
    val i: String,
    val noteId: String
)