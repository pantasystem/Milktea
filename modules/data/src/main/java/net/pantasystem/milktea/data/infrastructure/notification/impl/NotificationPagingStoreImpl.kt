package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.FuturePagingController
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.NotificationPagingStore
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.NotificationRelationGetter
import javax.inject.Inject

class NotificationPagingStoreImpl(
    val getAccount: suspend () -> Account,
    val delegate: NotificationStoreImpl,
    val unreadNotificationDAO: UnreadNotificationDAO,
    private val notificationRelationGetter: NotificationRelationGetter,
) : NotificationPagingStore {

    class Factory @Inject constructor(
        private val factory: NotificationStoreImpl.Factory,
        private val unreadNotificationDAO: UnreadNotificationDAO,
        private val notificationRelationGetter: NotificationRelationGetter,
    ) : NotificationPagingStore.Factory {
        override fun create(getAccount: suspend () -> Account): NotificationPagingStore {
            return NotificationPagingStoreImpl(
                getAccount,
                delegate = factory.create(getAccount),
                unreadNotificationDAO = unreadNotificationDAO,
                notificationRelationGetter = notificationRelationGetter,
            )
        }
    }

    private val previousPagingController = PreviousPagingController(
        entityConverter = delegate,
        locker = delegate,
        state = delegate,
        previousLoader = delegate,
    )

    private val futurePagingController = FuturePagingController(
        entityConverter = delegate,
        locker = delegate,
        state = delegate,
        futureLoader = delegate,
    )

    override val notifications: Flow<PageableState<List<NotificationRelation>>> =
        delegate.state.map { state ->
            state.suspendConvert { list ->
                list.map {
                    notificationRelationGetter.get(it)
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

    override suspend fun loadPrevious(): Result<Int> = runCancellableCatching {
        if (delegate.mutex.isLocked) {
            return@runCancellableCatching -1
        }
        unreadNotificationDAO.deleteWhereAccountId(getAccount().accountId)
        previousPagingController.loadPrevious().getOrThrow()
    }

    override suspend fun loadFuture(): Result<Int> = runCancellableCatching  {
        if (delegate.mutex.isLocked) {
            return@runCancellableCatching -1
        }
        futurePagingController.loadFuture().getOrThrow()
    }

    override suspend fun onReceiveNewNotification(notificationRelation: NotificationRelation) {
        delegate.mutex.withLock {
            val state = delegate.getState()
            val updated = when (state.content) {
                is StateContent.Exist -> state.convert {
                    listOf(
                        notificationRelation.notification.id
                    ) + it
                }
                is StateContent.NotExist -> state
            }
            delegate.setState(updated)
        }
    }
}



@kotlinx.serialization.Serializable
sealed class NotificationItem {
    abstract val nextId: String?
    abstract val id: String
    abstract val accountId: Long

    @kotlinx.serialization.Serializable
    data class Misskey(
        val notificationDTO: NotificationDTO,
        override val nextId: String?,
        override val id: String,
        override val accountId: Long
    ) : NotificationItem()

    @kotlinx.serialization.Serializable
    data class Mastodon(
        val mstNotificationDTO: MstNotificationDTO,
        override val nextId: String?,
        override val id: String,
        override val accountId: Long
    ) : NotificationItem()
}

data class NotificationAndNextId(
    val notification: NotificationRelation,
    val nextId: String?,
)