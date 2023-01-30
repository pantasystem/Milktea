package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class V2FilterDTO(
    val id: String,
    val title: String,
    val context: List<FilterContext>,
    @SerialName("expires_at") val expiresAt: Instant? = null,
    @SerialName("filter_action") val filterAction: FilterAction,
    val keywords: List<FilterKeyword>,
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
        val id: String,
        val keyword: String,
        @SerialName("whole_word") val wholeWord: Boolean,
    )

    @kotlinx.serialization.Serializable
    data class FilterStatus(
        val id: String,
        @SerialName("status_id") val statusId: String
    )
}