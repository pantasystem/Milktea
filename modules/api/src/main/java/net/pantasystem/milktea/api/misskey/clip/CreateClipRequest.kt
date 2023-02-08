package net.pantasystem.milktea.api.misskey.clip

@kotlinx.serialization.Serializable
data class CreateClipRequest(
    val i: String,
    val name: String,
    val isPublic: Boolean,
    val description: String? = null,
)

@kotlinx.serialization.Serializable
data class UpdateClipRequest(
    val i: String,
    val clipId: String,
    val name: String,
    val isPublic: Boolean,
    val description: String? = null,
)