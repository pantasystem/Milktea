package net.pantasystem.milktea.api.misskey.notes.translation

import kotlinx.serialization.Serializable

@Serializable
data class TranslationResult (val sourceLang: String, val text: String)