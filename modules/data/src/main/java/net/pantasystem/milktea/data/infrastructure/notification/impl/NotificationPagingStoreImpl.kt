package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.NotificationPagingStore
import net.pantasystem.milktea.model.notification.NotificationRelation
import javax.inject.Inject
import javax.inject.Singleton

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


class NotificationStoreImpl(
    private val getAccount: suspend () -> Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val notificationCacheAdder: NotificationCacheAdder,
) : StateLocker, PreviousLoader<NotificationItem>, PaginationState<NotificationAndNextId>,
    IdGetter<String>, EntityConverter<NotificationItem, NotificationAndNextId> {

    @Singleton
    class Factory @Inject constructor(
        private val misskeyAPIProvider: MisskeyAPIProvider,
        private val mastodonAPIProvider: MastodonAPIProvider,
        private val notificationCacheAdder: NotificationCacheAdder,
    ) {
        fun create(getAccount: suspend () -> Account): NotificationStoreImpl {
            return NotificationStoreImpl(
                getAccount = getAccount,
                misskeyAPIProvider = misskeyAPIProvider,
                mastodonAPIProvider = mastodonAPIProvider,
                notificationCacheAdder = notificationCacheAdder
            )
        }
    }
    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<NotificationAndNextId>>>(
        PageableState.Loading.Init()
    )
    override val state: Flow<PageableState<List<NotificationAndNextId>>>
        get() = _state

    override fun getState(): PageableState<List<NotificationAndNextId>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<NotificationAndNextId>>) {
        _state.value = state
    }

    override suspend fun loadPrevious(): Result<List<NotificationItem>> = runCancellableCatching {
        val account = getAccount()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                MisskeyNotificationEntityLoader(
                    account = account,
                    idGetter = this,
                    misskeyAPIProvider = misskeyAPIProvider,
                )
            }
            Account.InstanceType.MASTODON -> {
                MstNotificationEntityLoader(
                    account = account,
                    idGetter = this,
                    state = this,
                    mastodonAPIProvider = mastodonAPIProvider,
                )
            }
        }.loadPrevious().getOrThrow()
    }

    override suspend fun convertAll(list: List<NotificationItem>): List<NotificationAndNextId> {
        val account = getAccount()
        return list.mapNotNull {
            when(it) {
                is NotificationItem.Mastodon -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addConvert(account, it.mstNotificationDTO),
                            it.nextId
                        )
                    }.getOrNull()

                }
                is NotificationItem.Misskey -> {
                    runCancellableCatching {
                        NotificationAndNextId(
                            notificationCacheAdder.addAndConvert(account, it.notificationDTO),
                            it.nextId
                        )
                    }.getOrNull()
                }
            }
        }
    }

    override suspend fun getSinceId(): String? {
        return null
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.nextId
    }
}


class MstNotificationEntityLoader(
    val account: Account,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val idGetter: IdGetter<String>,
    val state: PaginationState<NotificationAndNextId>,
) : PreviousLoader<NotificationItem> {
    override suspend fun loadPrevious(): Result<List<NotificationItem>> = runCancellableCatching {
        val nextId = idGetter.getUntilId()
        val isEmpty = (state.getState().content as? StateContent.Exist)?.rawContent.isNullOrEmpty()
        if (nextId == null && !isEmpty) {
            return@runCancellableCatching emptyList()
        }
        val res = mastodonAPIProvider.get(account).getNotifications(
            maxId = nextId
        ).throwIfHasError()

        val body = res.body()

        val maxId = MastodonLinkHeaderDecoder(res.headers()["link"]).getMaxId()

        requireNotNull(body).map {
            NotificationItem.Mastodon(
                it,
                maxId
            )
        }
    }
}

class MisskeyNotificationEntityLoader(
    val account: Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val idGetter: IdGetter<String>,
) : PreviousLoader<NotificationItem> {
    override suspend fun loadPrevious(): Result<List<NotificationItem>> = runCancellableCatching {
        val res = misskeyAPIProvider.get(account).notification(
            NotificationRequest(
                i = account.token,
                untilId = idGetter.getUntilId()
            )
        )
        requireNotNull(res.body()).map {
            NotificationItem.Misskey(
                it,
                it.id,
            )
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