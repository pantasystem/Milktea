package net.pantasystem.milktea.api.milktea

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class InstanceInfoResponse(
    @SerialName("id")
    val id: String,

    @SerialName("host")
    val host: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("clientMaxBodyByteSize")
    val clientMaxBodyByteSize: Long? = null,

    @SerialName("iconUrl")
    val iconUrl: String? = null,

    @SerialName("themeColor")
    val themeColor: String? = null,
)
