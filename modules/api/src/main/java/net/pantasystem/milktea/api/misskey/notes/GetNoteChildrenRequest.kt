package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class GetNoteChildrenRequest(
    @SerialName("i") val i: String,
    @SerialName("noteId") val noteId: String,
    @SerialName("limit") val limit: Int,
    @SerialName("depth") val depth: Int,
)