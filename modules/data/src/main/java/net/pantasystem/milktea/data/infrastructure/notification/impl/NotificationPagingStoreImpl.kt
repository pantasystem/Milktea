package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.IdPreviousLoader
import net.pantasystem.milktea.common.paginator.MediatorPreviousPagingController
import net.pantasystem.milktea.common.paginator.PreviousCacheSaver
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecord
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationJsonCacheRecordDAO
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
    val notificationJsonCacheRecordDAO: NotificationJsonCacheRecordDAO,
) : NotificationPagingStore {

    class Factory @Inject constructor(
        private val factory: NotificationStoreImpl.Factory,
        private val unreadNotificationDAO: UnreadNotificationDAO,
        private val notificationCacheAdder: NotificationCacheAdder,
        private val notificationJsonCacheRecordDAO: NotificationJsonCacheRecordDAO,
    ) : NotificationPagingStore.Factory {
        override fun create(getAccount: suspend () -> Account): NotificationPagingStore {
            return NotificationPagingStoreImpl(
                getAccount,
                delegate = factory.create(getAccount),
                unreadNotificationDAO = unreadNotificationDAO,
                notificationCacheAdder = notificationCacheAdder,
                notificationJsonCacheRecordDAO = notificationJsonCacheRecordDAO
            )
        }
    }

    private val cacheSaver = NotificationPreviousCacheSaver(
        getAccount,
        notificationJsonCacheRecordDAO
    )

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

    override val notifications: Flow<PageableState<List<NotificationRelation>>> =
        delegate.state.map { state ->
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
            val updated = when (state.content) {
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

class NotificationPreviousCacheSaver(
    private val getAccount: suspend () -> Account,
    private val notificationJsonCacheRecordDAO: NotificationJsonCacheRecordDAO
) : PreviousCacheSaver<String, NotificationItem>,
    IdPreviousLoader<String, NotificationJsonCacheRecord, NotificationAndNextId> {
    val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun savePrevious(key: String?, elements: List<NotificationItem>) {
        val firstEl = elements.firstOrNull()
        if (firstEl != null) {
            if (key == null) {
                notificationJsonCacheRecordDAO.deleteByNullKey(firstEl.accountId)
            } else {
                notificationJsonCacheRecordDAO.deleteByKey(firstEl.accountId, key)
            }
        }
        val records = elements.mapIndexed { index, element ->
            NotificationJsonCacheRecord(
                accountId = element.accountId,
                json = json.encodeToString(element),
                weight = index,
                notificationId = element.id,
                key = key
            )
        }
        notificationJsonCacheRecordDAO.insertAll(records)
    }

    override suspend fun loadPrevious(
        state: PageableState<List<NotificationAndNextId>>,
        id: String?
    ): Result<List<NotificationJsonCacheRecord>> = runCancellableCatching {
        if (id == null) {
            notificationJsonCacheRecordDAO.findByNullKey(getAccount().accountId)
        } else {
            notificationJsonCacheRecordDAO.findByKey(getAccount().accountId, id)
        }
    }
}

class NotificationRecordConverter constructor(
    val getAccount: suspend () -> Account,
    val notificationCacheAdder: NotificationCacheAdder,
) : EntityConverter<NotificationJsonCacheRecord, NotificationAndNextId> {
    val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun convertAll(list: List<NotificationJsonCacheRecord>): List<NotificationAndNextId> {
        return list.mapNotNull {
            when (val item = json.decodeFromString<NotificationItem>(it.json)) {
                is NotificationItem.Mastodon -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addConvert(
                                getAccount(),
                                item.mstNotificationDTO,
                                true
                            ),
                            item.nextId,
                        )
                    }.getOrNull()
                }
                is NotificationItem.Misskey -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addAndConvert(
                                getAccount(),
                                item.notificationDTO,
                                true
                            ),
                            item.nextId,
                        )
                    }.getOrNull()
                }
            }
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