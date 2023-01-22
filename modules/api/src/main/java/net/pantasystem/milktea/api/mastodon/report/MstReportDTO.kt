package net.pantasystem.milktea.api.mastodon.report

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountDTO

@kotlinx.serialization.Serializable
data class MstReportDTO(
    val id: String,
    @SerialName("action_taken") val actionTaken: Boolean? = null,
    @SerialName("action_taken_at") val actionTakenAt: Instant? = null,
    val category: String? = null,
    val comment: String? = null,
    val forwarded: Boolean? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("status_ids") val statusIds: List<String>? = null,
    @SerialName("rule_ids") val ruleIds: List<String>? = null,
    @SerialName("target_account") val targetAccount: MastodonAccountDTO? = null
)