package net.pantasystem.milktea.api.mastodon.rule

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class RuleDTO(
    @SerialName("id") val id: String,
    @SerialName("text") val text: String,
)