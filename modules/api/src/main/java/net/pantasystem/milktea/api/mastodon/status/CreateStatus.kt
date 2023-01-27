package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateStatus(
    val status: String,
    @SerialName("media_ids") val mediaIds: List<String>,
    val poll: CreatePoll? = null,
    @SerialName("in_reply_to_id") val inReplyToId: String? = null,
    val sensitive: Boolean = false,
    @SerialName("spoiler_text") val spoilerText: String? = null,
    val visibility: String = "public",
    val language: String? = null,
    @SerialName("scheduled_at") val scheduledAt: Instant? = null
) {

    @kotlinx.serialization.Serializable
    data class CreatePoll(
        val options: List<String>,
        @SerialName("expires_in") val expiresIn: Int,
        val multiple: Boolean = false,
        @SerialName("hide_totals") val hideTotals: Boolean = false,

    )
}
