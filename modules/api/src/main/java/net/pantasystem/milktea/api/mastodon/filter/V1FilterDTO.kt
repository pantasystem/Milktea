package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class V1FilterDTO(
    val id: String,
    val phrase: String,
    val context: List<FilterContext>,
    @SerialName("whole_word") val wholeWord: Boolean,
    @SerialName("expires_at") val expiresAt: Instant? = null,
    val irreversible: Boolean,
)