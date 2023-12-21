package net.pantasystem.milktea.api.mastodon.notification

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.report.MstReportDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class MstNotificationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: NotificationType,

    @SerialName("created_at")
    val createdAt: Instant,

    @SerialName("account")
    val account: MastodonAccountDTO,

    @SerialName("status")
    val status: TootStatusDTO? = null,

    @SerialName("report")
    val report: MstReportDTO? = null,

    @SerialName("emoji_reaction")
    val emojiReaction: EmojiReaction? = null,
) {
    @kotlinx.serialization.Serializable
    enum class NotificationType(
        val value: String
    ) {
        @SerialName("mention")
        Mention("mention"),

        @SerialName("status")
        Status("status"),

        @SerialName("reblog")
        Reblog("reblog"),

        @SerialName("follow")
        Follow("follow"),

        @SerialName("follow_request")
        FollowRequest("follow_request"),

        @SerialName("favourite")
        Favourite("favourite"),

        @SerialName("poll")
        Poll("poll"),

        @SerialName("update")
        Update("update"),

        @SerialName("admin.sign_up")
        AdminSingUp("admin.sign_up"),

        @SerialName("admin.report")
        AdminReport("admin.report"),

        @SerialName("emoji_reaction")
        EmojiReaction("emoji_reaction"),
    }

    @kotlinx.serialization.Serializable
    data class EmojiReaction(
        @SerialName("name")
        val name: String,

        @SerialName("count")
        val count: Int,

        @SerialName("me")
        val me: Boolean? = null,

        @SerialName("url")
        val url: String? = null,

        @SerialName("domain")
        val domain: String?  = null,

        @SerialName("static_url")
        val staticUrl: String? = null,
    ) {
        private val isCustomEmoji = url != null || staticUrl != null

        val reaction = if (isCustomEmoji) {
            if (domain == null) {
                ":$name@.:"
            } else {
                ":$name@$domain:"
            }
        } else {
            name
        }
    }
}

