package net.pantasystem.milktea.api.misskey.notes.reaction

import kotlinx.serialization.Serializable

@Serializable
data class RequestReactionHistoryDTO (
    val i: String,
    val noteId: String,
    val type: String?,
    val limit: Int = 20,
    val offset: Int? = null,
)