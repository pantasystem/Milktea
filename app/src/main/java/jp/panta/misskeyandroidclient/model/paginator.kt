package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

/**
 * ページネーションするためのIdを取得するためのインターフェース
 * IdはAPIやデータベースにアクセスするものと直接的に対応する
 */
interface IdGetter<T> {
    suspend fun getUntilId(): T?
    suspend fun getSinceId(): T?
}

/**
 * 状態をロックすることのできることを表すインターフェース
 */
interface StateLocker {
    val mutex: Mutex
}

interface PageableState<T> {
    val state: Flow<State<List<T>>>
    fun setState(state: State<List<T>>)
    fun getState(): State<List<T>>
}

interface FuturePaginator {
    suspend fun loadFuture()
}

interface PreviousPaginator {
    suspend fun loadPrevious()
}

interface EntityAdder<DTO, E> {
    suspend fun addAll(list: List<DTO>) : List<E>
}

interface PreviousLoader<DTO> {
    suspend fun loadPrevious(): Response<List<DTO>>
}

interface FutureLoader<DTO> {
    suspend fun loadFuture(): Response<List<DTO>>
}

class PreviousPaginatorController<DTO, E>(
    private val entityAdder: EntityAdder<DTO, E>,
    private val locker: StateLocker,
    private val state: PageableState<E>,
    private val previousLoader: PreviousLoader<DTO>
) : PreviousPaginator{
    override suspend fun loadPrevious() {
        locker.mutex.withLock {

            val loading = State.Loading(
                content = state.getState().content
            )
            state.setState(loading)
            runCatching {
                val res = previousLoader.loadPrevious().throwIfHasError()
                res.throwIfHasError()
                entityAdder.addAll(res.body()!!)
            }.onFailure {
                val errorState = State.Error(
                    state.getState().content,
                    it
                )
                state.setState(errorState)
            }.onSuccess {
                when(val content = this.state.getState().content) {
                    is StateContent.Exist -> {
                        val newList = content.rawContent.toMutableList()
                        newList.addAll(it)
                        state.setState(
                            State.Fixed(
                                StateContent.Exist(
                                    newList
                                )
                            )
                        )
                    }
                    is StateContent.NotExist -> {
                        state.setState(
                            State.Fixed(
                                StateContent.Exist(it)
                            )
                        )
                    }
                }
            }
        }
    }
}

class FuturePaginatorController<DTO, E>(
    private val entityAdder: EntityAdder<DTO, E>,
    private val locker: StateLocker,
    private val state: PageableState<E>,
    private val futureLoader: FutureLoader<DTO>
) : FuturePaginator{
    override suspend fun loadFuture() {
        locker.mutex.withLock {

            val loading = State.Loading(
                content = state.getState().content
            )
            state.setState(loading)
            runCatching {
                val res = futureLoader.loadFuture().throwIfHasError()
                res.throwIfHasError()
                entityAdder.addAll(res.body()!!).asReversed()
            }.onFailure {
                val errorState = State.Error(
                    state.getState().content,
                    it
                )
                state.setState(errorState)
            }.onSuccess {
                when(val content = this.state.getState().content) {
                    is StateContent.Exist -> {
                        val newList = content.rawContent.toMutableList()
                        newList.addAll(0, it)
                        state.setState(
                            State.Fixed(
                                StateContent.Exist(
                                    newList
                                )
                            )
                        )
                    }
                    is StateContent.NotExist -> {
                        state.setState(
                            State.Fixed(
                                StateContent.Exist(it)
                            )
                        )
                    }
                }
            }
        }
    }
}