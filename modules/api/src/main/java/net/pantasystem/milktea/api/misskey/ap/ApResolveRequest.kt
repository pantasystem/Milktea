package net.pantasystem.milktea.api.misskey.ap

@kotlinx.serialization.Serializable
data class ApResolveRequest(
    val i: String,
    val uri: String,
)
