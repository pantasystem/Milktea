package net.pantasystem.milktea.api.misskey.clip

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateClipRequest(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String,

    @SerialName("isPublic")
    val isPublic: Boolean,

    @SerialName("description")
    val description: String? = null,
)

@kotlinx.serialization.Serializable
data class UpdateClipRequest(
    @SerialName("i")
    val i: String,

    @SerialName("clipId")
    val clipId: String,

    @SerialName("name")
    val name: String,

    @SerialName("isPublic")
    val isPublic: Boolean,

    @SerialName("description")
    val description: String? = null,
)