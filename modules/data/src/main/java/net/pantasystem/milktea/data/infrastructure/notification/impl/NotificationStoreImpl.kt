package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.IdPreviousLoader
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject
import javax.inject.Singleton


class NotificationStoreImpl(
    private val getAccount: suspend () -> Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val notificationCacheAdder: NotificationCacheAdder,
) : StateLocker, IdPreviousLoader<String, NotificationItem, NotificationAndNextId>, PaginationState<NotificationAndNextId>,
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

    override suspend fun loadPrevious(state: PageableState<List<NotificationAndNextId>>, id: String?): Result<List<NotificationItem>> = runCancellableCatching {
        val account = getAccount()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                MisskeyNotificationEntityLoader(
                    account = account,
                    misskeyAPIProvider = misskeyAPIProvider,
                )
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                MstNotificationEntityLoader(
                    account = account,
                    mastodonAPIProvider = mastodonAPIProvider,
                )
            }
        }.loadPrevious(state, id).getOrThrow()
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
