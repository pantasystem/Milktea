package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class FilterResultDTO(
    @SerialName("filter")
    val filter: HitFilter,

    @SerialName("keyword_matches")
    val keywordMatches: List<String>? = null,

    @SerialName("status_matches")
    val statusMatches: List<String>? = null,
) {
    @kotlinx.serialization.Serializable
    data class HitFilter(
        @SerialName("id")
        val id: String,

        @SerialName("title")
        val title: String,

        @SerialName("context")
        val context: List<FilterContext>,

        @SerialName("filter_action")
        val filterAction: V2FilterDTO.FilterAction,
    )
}