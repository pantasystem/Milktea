package net.pantasystem.milktea.api.misskey.register

@kotlinx.serialization.Serializable
data class WebClientRegistries(
    val reactions: List<String>
)

@kotlinx.serialization.Serializable
data class WebClientBaseRequest(
    val i: String,
    val scope: List<String>
)