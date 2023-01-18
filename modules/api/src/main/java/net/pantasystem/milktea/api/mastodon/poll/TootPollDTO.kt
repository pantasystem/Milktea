package net.pantasystem.milktea.api.mastodon.poll

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO

@kotlinx.serialization.Serializable
data class TootPollDTO(
    val id: String,
    @SerialName("expires_at") val expiresAt: Instant?,
    val expired: Boolean,
    val multiple: Boolean,
    @SerialName("votes_count") val votesCount: Int,
    @SerialName("voters_count") val votersCount: Int,
    val options: List<Option>,
    val emojis: List<TootEmojiDTO>,
    val voted: Boolean?,
    val ownVotes: Boolean?,
) {

    @kotlinx.serialization.Serializable
    data class Option(
        val title: String,
        @SerialName("votes_count") val votesCount: Int?,
    )
}