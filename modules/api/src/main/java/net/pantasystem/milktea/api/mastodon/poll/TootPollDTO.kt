package net.pantasystem.milktea.api.mastodon.poll

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO

@kotlinx.serialization.Serializable
data class TootPollDTO(
    val id: String,
    @SerialName("expires_at") val expiresAt: Instant? = null,
    val expired: Boolean? = null,
    val multiple: Boolean? = null,
    @SerialName("votes_count") val votesCount: Int? = null,
    @SerialName("voters_count") val votersCount: Int? = null,
    val options: List<Option>,
    val emojis: List<TootEmojiDTO>,
    val voted: Boolean? = null,
    val ownVotes: List<Int>? = null,
) {

    @kotlinx.serialization.Serializable
    data class Option(
        val title: String,
        @SerialName("votes_count") val votesCount: Int? = null,
    )
}