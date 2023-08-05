package net.pantasystem.milktea.model.user.follow.requests

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject


class FollowRequestPagingStore(
    val getAccount: suspend () -> Account,
    followRequestRepository: FollowRequestRepository,
){

    class Factory @Inject constructor(
        private val followRequestRepository: FollowRequestRepository
    ) {
        fun create(getAccount: suspend () -> Account): FollowRequestPagingStore {
            return FollowRequestPagingStore(getAccount, followRequestRepository)
        }
    }

    private val impl = FollowRequestPagingStoreImpl(getAccount, followRequestRepository)
    private val previousController = PreviousPagingController(
        impl,
        impl,
        impl,
        impl,
    )

    val state = impl.state

    suspend fun loadPrevious(): Result<Unit> = runCancellableCatching {
        previousController.loadPrevious().getOrThrow()
    }
    suspend fun clear() {
        impl.mutex.withLock {
            impl.setState(PageableState.Loading.Init())
            impl.untilId = null
            impl.sinceId = null
        }
    }
}

class FollowRequestPagingStoreImpl(
    val getAccount: suspend () -> Account,
    private val followRequestRepository: FollowRequestRepository,
): StateLocker, PaginationState<User.Id>, PreviousLoader<User>, EntityConverter<User, User.Id>, IdGetter<String> {


    override val mutex: Mutex = Mutex()

    private val _state = MutableStateFlow<PageableState<List<User.Id>>>(PageableState.Loading.Init())

    var sinceId: String? = null
    var untilId: String? = null

    override val state: Flow<PageableState<List<User.Id>>>
        get() = _state

    override suspend fun convertAll(list: List<User>): List<User.Id> {
        return list.map {
            it.id
        }
    }

    override fun getState(): PageableState<List<User.Id>> {
        return _state.value
    }

    override suspend fun loadPrevious(): Result<List<User>> = runCancellableCatching {
        val result = followRequestRepository.find(
            getAccount().accountId,
            untilId = getUntilId()
        )
        untilId = result.untilId
        result.users
    }

    override fun setState(state: PageableState<List<User.Id>>) {
        _state.value = state
    }

    override suspend fun getSinceId(): String? {
        return sinceId
    }

    override suspend fun getUntilId(): String? {
        return untilId
    }
}