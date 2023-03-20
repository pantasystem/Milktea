package net.pantasystem.milktea.api.misskey.hashtag

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SearchHashtagRequest(
    @SerialName("query")
    val query: String,

    @SerialName("limit")
    val limit: Int = 10,

    @SerialName("offset")
    val offset: Int = 0
)