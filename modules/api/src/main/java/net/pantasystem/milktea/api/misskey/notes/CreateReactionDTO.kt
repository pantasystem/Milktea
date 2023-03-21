package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateReactionDTO(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String,

    @SerialName("reaction")
    val reaction: String,
)