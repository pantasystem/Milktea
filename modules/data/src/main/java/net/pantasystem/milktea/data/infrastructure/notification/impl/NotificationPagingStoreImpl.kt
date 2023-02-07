package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.IdPreviousLoader
import net.pantasystem.milktea.common.paginator.MediatorPreviousPagingController
import net.pantasystem.milktea.common.paginator.PreviousCacheSaver
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
    val notificationCacheAdder: NotificationCacheAdder,
) : NotificationPagingStore {

    class Factory @Inject constructor(
        val factory: NotificationStoreImpl.Factory,
        val unreadNotificationDAO: UnreadNotificationDAO,
        val notificationCacheAdder: NotificationCacheAdder,
    ) : NotificationPagingStore.Factory {
        override fun create(getAccount: suspend () -> Account): NotificationPagingStore {
            return NotificationPagingStoreImpl(
                getAccount,
                delegate = factory.create(getAccount),
                unreadNotificationDAO = unreadNotificationDAO,
                notificationCacheAdder = notificationCacheAdder,
            )
        }
    }

    val cacheSaver = NotificationPreviousCacheSaver(getAccount)

    val previousPagingController = MediatorPreviousPagingController(
        entityConverter = delegate,
        locker = delegate,
        state = delegate,
        idGetter = delegate,
        previousCacheSaver = cacheSaver,
        localPreviousLoader = cacheSaver,
        localRecordConverter = NotificationRecordConverter(getAccount, notificationCacheAdder),
        previousLoader = delegate,
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

// TODO: データベース上に実装する
class NotificationPreviousCacheSaver(
    val getAccount: suspend () -> Account,
) : PreviousCacheSaver<String, NotificationItem>, IdPreviousLoader<String, NotificationCacheRecord, NotificationAndNextId> {

    private data class Key(val key: String?, val accountId: Long)
    private var lists = mutableMapOf<Key, List<NotificationCacheRecord>>()


    override suspend fun savePrevious(key: String?, elements: List<NotificationItem>) {
        val accountId = getAccount().accountId
        lists[Key(key, accountId)] = elements.map {
            NotificationCacheRecord(key, accountId, it)
        }
    }

    override suspend fun loadPrevious(
        state: PageableState<List<NotificationAndNextId>>,
        id: String?
    ): Result<List<NotificationCacheRecord>> = runCancellableCatching {
        lists[Key(id, getAccount().accountId)] ?: emptyList()
    }
}

class NotificationRecordConverter constructor(
    val getAccount: suspend () -> Account,
    val notificationCacheAdder: NotificationCacheAdder,
): EntityConverter<NotificationCacheRecord, NotificationAndNextId> {
    override suspend fun convertAll(list: List<NotificationCacheRecord>): List<NotificationAndNextId> {
        return list.mapNotNull {
            when(it.item) {
                is NotificationItem.Mastodon -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addConvert(getAccount(), it.item.mstNotificationDTO, true),
                            it.item.nextId,
                        )
                    }.getOrNull()
                }
                is NotificationItem.Misskey -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addAndConvert(getAccount(), it.item.notificationDTO, true),
                            it.item.nextId,
                        )
                    }.getOrNull()
                }
            }
        }
    }
}


data class NotificationCacheRecord(
    val key: String?,
    val accountId: Long,
    val item: NotificationItem,
)

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