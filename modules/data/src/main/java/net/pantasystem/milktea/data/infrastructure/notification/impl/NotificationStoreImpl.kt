package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.FutureLoader
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationTimelineRepository
import javax.inject.Inject
import javax.inject.Singleton


class NotificationStoreImpl(
    private val getAccount: suspend () -> Account,
    private val notificationTimelineRepository: NotificationTimelineRepository,
) : StateLocker, PreviousLoader<Notification>, PaginationState<Notification.Id>, FutureLoader<Notification>,
    IdGetter<String>, EntityConverter<Notification, Notification.Id> {

    @Singleton
    class Factory @Inject constructor(
        private val notificationTimelineRepository: NotificationTimelineRepository,
    ) {
        fun create(getAccount: suspend () -> Account): NotificationStoreImpl {
            return NotificationStoreImpl(
                getAccount = getAccount,
                notificationTimelineRepository = notificationTimelineRepository,
            )
        }
    }
    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<Notification.Id>>>(
        PageableState.Loading.Init()
    )
    override val state: Flow<PageableState<List<Notification.Id>>>
        get() = _state

    override fun getState(): PageableState<List<Notification.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Notification.Id>>) {
        _state.value = state
    }


    override suspend fun convertAll(list: List<Notification>): List<Notification.Id> {
        return list.map {
            it.id
        }.distinct()
    }

    override suspend fun loadPrevious(): Result<List<Notification>> = runCancellableCatching {
        val account = getAccount()
        notificationTimelineRepository.findPreviousTimeline(
            accountId = account.accountId,
            untilId = getUntilId(),
        ).getOrThrow()
    }

    override suspend fun loadFuture(): Result<List<Notification>> = runCancellableCatching{
        val account = getAccount()
        notificationTimelineRepository.findLaterTimeline(
            accountId = account.accountId,
            sinceId = getSinceId(),
        ).getOrThrow()
    }

    override suspend fun getSinceId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.firstOrNull()?.notificationId
    }

    override suspend fun getUntilId(): String? {
        return (_state.value.content as? StateContent.Exist)?.rawContent?.lastOrNull()?.notificationId
    }
}
