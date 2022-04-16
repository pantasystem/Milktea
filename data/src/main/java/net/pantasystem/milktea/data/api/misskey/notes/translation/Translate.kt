package net.pantasystem.milktea.data.api.misskey.notes.translation

import kotlinx.serialization.Serializable

@Serializable
data class Translate (
    val i: String,
    val noteId: String,
    val targetLang: String
)