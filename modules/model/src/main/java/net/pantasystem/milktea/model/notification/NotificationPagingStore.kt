package net.pantasystem.milktea.model.notification

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account

interface NotificationPagingStore {

    interface Factory {
        fun create(getAccount: suspend () -> Account): NotificationPagingStore
    }

    val notifications: Flow<PageableState<List<NotificationRelation>>>

    suspend fun loadPrevious(): Result<Unit>

    suspend fun clear()
}