package net.pantasystem.milktea.api.misskey.notes.translation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranslationResult(
    @SerialName("sourceLang")
    val sourceLang: String,

    @SerialName("text")
    val text: String,
)