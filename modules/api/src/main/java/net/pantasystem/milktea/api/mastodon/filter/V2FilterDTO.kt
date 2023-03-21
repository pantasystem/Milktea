package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class V2FilterDTO(
    @SerialName("id")
    val id: String,

    @SerialName("title")
    val title: String,

    @SerialName("context")
    val context: List<FilterContext>,

    @SerialName("expires_at")
    val expiresAt: Instant? = null,

    @SerialName("filter_action")
    val filterAction: FilterAction,

    @SerialName("keywords")
    val keywords: List<FilterKeyword>,

    @SerialName("statuses")
    val statuses: List<FilterStatus>,
) {
    @kotlinx.serialization.Serializable
    enum class FilterAction {
        @SerialName("warn")
        Warn,
        @SerialName("hide")
        Hide
    }

    @kotlinx.serialization.Serializable
    data class FilterKeyword(
        @SerialName("id")
        val id: String,

        @SerialName("keyword")
        val keyword: String,

        @SerialName("whole_word")
        val wholeWord: Boolean,
    )

    @kotlinx.serialization.Serializable
    data class FilterStatus(
        @SerialName("id")
        val id: String,

        @SerialName("status_id")
        val statusId: String
    )
}