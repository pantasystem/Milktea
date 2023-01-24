package net.pantasystem.milktea.model.notification

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account

interface NotificationPagingStore {

    interface Factory {
        fun create(getAccount: () -> Account): NotificationPagingStore
    }

    val notifications: Flow<PageableState<Notification>>

    fun loadPrevious(): Result<Unit>

    fun clear(): Result<Unit>
}