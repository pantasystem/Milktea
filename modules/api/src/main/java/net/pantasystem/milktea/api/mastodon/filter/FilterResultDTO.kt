package net.pantasystem.milktea.api.mastodon.filter

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class FilterResultDTO(
    val filter: HitFilter,
    @SerialName("keyword_matches") val keywordMatches: List<String>,
    @SerialName("status_matches") val statusMatches: List<String>,
) {
    @kotlinx.serialization.Serializable
    data class HitFilter(
        val id: String,
        val title: String,
        val context: List<FilterContext>,
        val filterAction: V2FilterDTO.FilterAction,
    )
}