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
    }
}

