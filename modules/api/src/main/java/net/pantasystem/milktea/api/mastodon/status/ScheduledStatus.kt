package net.pantasystem.milktea.api.mastodon.status

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment

@kotlinx.serialization.Serializable
data class ScheduledStatus(
    @SerialName("id")
    val id: String,

    @SerialName("scheduled_at")
    val scheduledAt: Instant,

    @SerialName("params")
    val params: Params,

    @SerialName("media_attachments")
    val mediaAttachments: List<TootMediaAttachment>? = null
) {

    @kotlinx.serialization.Serializable
    data class Params(
        @SerialName("text")
        val text: String,

        @SerialName("poll")
        val poll: CreateStatus.CreatePoll? = null,

        @SerialName("media_ids")
        val mediaIds: List<String>? = null,

        @SerialName("sensitive")
        val sensitive: Boolean? = null,

        @SerialName("spoiler_text")
        val spoilerText: String? = null,

        @SerialName("visibility")
        val visibility: String,

        @SerialName("in_reply_to_id")
        val inReplyToId: String? = null,

        @SerialName("language")
        val language: String? = null,

        @SerialName("application_id")
        val applicationId: Int? = null,

        @SerialName("scheduled_at")
        val scheduledAt: Instant? = null,

        @SerialName("with_rate_limit")
        val withRateLimit: Boolean? = null,
    )
}
