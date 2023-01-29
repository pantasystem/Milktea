package net.pantasystem.milktea.api.mastodon.notification

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO
import net.pantasystem.milktea.api.mastodon.report.MstReportDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO

@kotlinx.serialization.Serializable
data class MstNotificationDTO(
    val id: String,
    val type: NotificationType,
    @SerialName("created_at") val createdAt: Instant,
    val account: MastodonAccountDTO,
    val status: TootStatusDTO? = null,
    val report: MstReportDTO? = null,
    @SerialName("emoji_reaction") val emojiReaction: EmojiReaction? = null,
) {
    @kotlinx.serialization.Serializable
    enum class NotificationType {
        @SerialName("mention")
        Mention,

        @SerialName("status")
        Status,

        @SerialName("reblog")
        Reblog,

        @SerialName("follow")
        Follow,

        @SerialName("follow_request")
        FollowRequest,

        @SerialName("favourite")
        Favourite,

        @SerialName("poll")
        Poll,

        @SerialName("update")
        Update,

        @SerialName("admin.sign_up")
        AdminSingUp,

        @SerialName("admin.report")
        AdminReport,

        @SerialName("emoji_reaction")
        EmojiReaction
    }

    @kotlinx.serialization.Serializable
    data class EmojiReaction(
        val name: String,
        val count: Int,
        val me: Boolean? = null,
        val url: String? = null,
        val domain: String?  = null,
        @SerialName("static_url") val staticUrl: String? = null,
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

