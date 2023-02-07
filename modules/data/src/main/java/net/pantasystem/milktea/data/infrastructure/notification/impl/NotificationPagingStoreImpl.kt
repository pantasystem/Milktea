package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.NotificationPagingStore
import net.pantasystem.milktea.model.notification.NotificationRelation
import javax.inject.Inject

class NotificationPagingStoreImpl(
    val getAccount: suspend () -> Account,
    val delegate: NotificationStoreImpl,
    val unreadNotificationDAO: UnreadNotificationDAO,
) : NotificationPagingStore {

    class Factory @Inject constructor(
        val factory: NotificationStoreImpl.Factory,
        val unreadNotificationDAO: UnreadNotificationDAO,
    ) : NotificationPagingStore.Factory {
        override fun create(getAccount: suspend () -> Account): NotificationPagingStore {
            return NotificationPagingStoreImpl(
                getAccount,
                delegate = factory.create(getAccount),
                unreadNotificationDAO = unreadNotificationDAO,
            )
        }
    }

    val previousPagingController = PreviousPagingController(
        delegate,
        delegate,
        delegate,
        delegate
    )

    override val notifications: Flow<PageableState<List<NotificationRelation>>> = delegate.state.map { state ->
        state.convert { list ->
            list.map {
                it.notification
            }
        }
    }

    override suspend fun clear() {
        delegate.mutex.withLock {
            delegate.setState(
                PageableState.Loading.Init()
            )
        }
    }

    override suspend fun loadPrevious(): Result<Unit> = runCancellableCatching {
        unreadNotificationDAO.deleteWhereAccountId(getAccount().accountId)
        previousPagingController.loadPrevious().getOrThrow()
    }

    override suspend fun onReceiveNewNotification(notificationRelation: NotificationRelation) {
        delegate.mutex.withLock {
            val state = delegate.getState()
            val updated = when(state.content) {
                is StateContent.Exist -> state.convert {
                    listOf(
                        NotificationAndNextId(notificationRelation, null)
                    ) + it
                }
                is StateContent.NotExist -> state
            }
            delegate.setState(updated)

        }
    }
}




sealed interface NotificationItem {
    val nextId: String?

    data class Misskey(
        val notificationDTO: NotificationDTO,
        override val nextId: String?,
    ) : NotificationItem

    data class Mastodon(
        val mstNotificationDTO: MstNotificationDTO,
        override val nextId: String?,
    ) : NotificationItem
}

data class NotificationAndNextId(
    val notification: NotificationRelation,
    val nextId: String?,
)