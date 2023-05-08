package net.pantasystem.milktea.api.mastodon.tag

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.hashtag.HashTag

@kotlinx.serialization.Serializable
data class MastodonTagDTO(
    @SerialName("name") val name: String,
    @SerialName("url") val url: String,
    @SerialName("history") val history: List<History>,
) {
    @kotlinx.serialization.Serializable
    data class History(
        @SerialName("day") val day: Long,
        @SerialName("uses") val uses: Int,
        @SerialName("accounts") val accounts: Int,
    )

    fun toModel(): HashTag {
        return HashTag(
            name,
            history.sumOf { it.uses },
            history.map { it.uses }
        )
    }
}