package net.pantasystem.milktea.api.mastodon.poll

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.emojis.TootEmojiDTO

@kotlinx.serialization.Serializable
data class TootPollDTO(
    @SerialName("id")
    val id: String,

    @SerialName("expires_at")
    val expiresAt: Instant? = null,

    @SerialName("expired")
    val expired: Boolean? = null,

    @SerialName("multiple")
    val multiple: Boolean? = null,

    @SerialName("votes_count")
    val votesCount: Int? = null,

    @SerialName("voters_count")
    val votersCount: Int? = null,

    @SerialName("options")
    val options: List<Option>,

    @SerialName("emojis")
    val emojis: List<TootEmojiDTO>,

    @SerialName("voted")
    val voted: Boolean? = null,

    @SerialName("own_votes")
    val ownVotes: List<Int>? = null,
) {

    @kotlinx.serialization.Serializable
    data class Option(
        @SerialName("title")
        val title: String,

        @SerialName("votes_count")
        val votesCount: Int? = null,
    )
}