package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FindRenotes(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String,

    @SerialName("untilId")
    val untilId: String? = null,

    @SerialName("sinceId")
    val sinceId: String? = null,
)
