package net.pantasystem.milktea.api.misskey.notes.reaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestReactionHistoryDTO(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String,

    @SerialName("type")
    val type: String?,

    @SerialName("limit")
    val limit: Int = 20,

    @SerialName("offset")
    val offset: Int? = null,
)