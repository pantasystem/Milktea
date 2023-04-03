package net.pantasystem.milktea.api.mastodon.report

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateReportRequest(
    @SerialName("account_id") val accountId: String,
    @SerialName("status_ids") val statusIds: List<String>,
    @SerialName("comment") val comment: String? = null,
    @SerialName("forward") val forward: Boolean = false,
    @SerialName("category") val category: Category = Category.Other,
    @SerialName("rule_ids") val ruleIds: List<String>? = null,
) {

    @kotlinx.serialization.Serializable
    enum class Category {
        @SerialName("spam") Spam,
        @SerialName("violation") Violation,
        @SerialName("other") Other,
    }
}