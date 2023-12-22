package net.pantasystem.milktea.api.misskey.infos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimpleInstanceInfo(
    @SerialName("uri") val url: String,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("iconUrl") val iconUrl: String? = null,
)