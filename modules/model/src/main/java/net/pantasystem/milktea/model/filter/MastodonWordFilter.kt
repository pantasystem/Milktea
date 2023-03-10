package net.pantasystem.milktea.model.filter

import kotlinx.datetime.Instant

data class MastodonWordFilter(
    val id: Id,
    val phrase: String,
    val context: List<FilterContext>,
    val wholeWord: Boolean,
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

    val isContextHome = context.any {
        it == FilterContext.Home
    }

    val isContextNotifications = context.any {
        it == FilterContext.Notifications
    }

    val isContextPublic = context.any {
        it == FilterContext.Public
    }

    val isContextThread = context.any {
        it == FilterContext.Thread
    }

    val isContextAccount = context.any {
        it == FilterContext.Account
    }
}