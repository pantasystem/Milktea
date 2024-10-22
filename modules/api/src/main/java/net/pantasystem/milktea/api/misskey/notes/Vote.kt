package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    @SerialName("i") val i: String,
    @SerialName("choice") val choice: Int,
    @SerialName("noteId") val noteId: String
)