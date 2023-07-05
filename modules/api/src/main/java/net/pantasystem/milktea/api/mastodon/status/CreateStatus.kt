package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateStatus(
    @SerialName("status")
    val status: String,

    @SerialName("media_ids")
    val mediaIds: List<String>,

    @SerialName("poll")
    val poll: CreatePoll? = null,

    @SerialName("in_reply_to_id")
    val inReplyToId: String? = null,

    @SerialName("sensitive")
    val sensitive: Boolean = false,

    @SerialName("spoiler_text")
    val spoilerText: String? = null,

    @SerialName("visibility")
    val visibility: String = "public",

    @SerialName("language")
    val language: String? = null,

    @SerialName("scheduled_at")
    val scheduledAt: Instant? = null,

    @SerialName("quote_id")
    val quoteId: String? = null,
) {

    @kotlinx.serialization.Serializable
    data class CreatePoll(
        @SerialName("options")
        val options: List<String>,

        @SerialName("expires_in")
        val expiresIn: Int,

        @SerialName("multiple")
        val multiple: Boolean = false,

        @SerialName("hide_totals")
        val hideTotals: Boolean = false,

    )
}
