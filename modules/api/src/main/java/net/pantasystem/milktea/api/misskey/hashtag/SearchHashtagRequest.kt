package net.pantasystem.milktea.api.misskey.hashtag

@kotlinx.serialization.Serializable
data class SearchHashtagRequest(
    val query: String,
    val limit: Int = 10,
    val offset: Int = 0
)