package net.pantasystem.milktea.api.misskey.notes.translation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Translate(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String,

    @SerialName("targetLang")
    val targetLang: String
)