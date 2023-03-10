package net.pantasystem.milktea.model.filter

import kotlinx.datetime.Instant

data class MastodonWordFilter(
    val id: Id,
    val phrase: String,
    val context: List<FilterContext>,
    val isWholeWord: Boolean,
    val expiresAt: Instant?,
    val irreversible: Boolean,
) {
    data class Id(
        val accountId: Long,
        val filterId: String
    )
    enum class FilterContext {
        Home,
        Notifications,
        Public,
        Thread,
        Account,
    }
}