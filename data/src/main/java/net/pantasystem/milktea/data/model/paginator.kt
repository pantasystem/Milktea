package net.pantasystem.milktea.data.model

import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
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

interface PaginationState<T> {
    val state: Flow<PageableState<List<T>>>
    fun setState(state: PageableState<List<T>>)
    fun getState(): PageableState<List<T>>
}

interface FuturePaginator {
    suspend fun loadFuture()
}

interface PreviousPaginator {
    suspend fun loadPrevious()
}


/**
 * DTOをEntityに変換し共通のDataStoreにEntityを追加するためのInterface
 */
interface EntityConverter<DTO, E> {
    suspend fun convertAll(list: List<DTO>) : List<E>
}

/**
 * 過去のページを取得するためのAPIのアダプター
 */
interface PreviousLoader<DTO> {
    suspend fun loadPrevious(): Response<List<DTO>>
}

/**
 * 新たなページを取得するためのAPIのアダプター
 */
interface FutureLoader<DTO> {
    suspend fun loadFuture(): Response<List<DTO>>
}

class PreviousPagingController<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val previousLoader: PreviousLoader<DTO>
) : PreviousPaginator {
    override suspend fun loadPrevious() {
        locker.mutex.withLock {

            val loading = PageableState.Loading.Previous(
                content = state.getState().content
            )
            state.setState(loading)
            runCatching {
                val res = previousLoader.loadPrevious().throwIfHasError()
                entityConverter.convertAll(res.body()!!)
            }.onFailure {
                val errorState = PageableState.Error(
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
                            PageableState.Fixed(
                                StateContent.Exist(
                                    newList
                                )
                            )
                        )
                    }
                    is StateContent.NotExist -> {
                        state.setState(
                            PageableState.Fixed(
                                StateContent.Exist(it)
                            )
                        )
                    }
                }
            }
        }
    }
}

class FuturePagingController<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val futureLoader: FutureLoader<DTO>
) : FuturePaginator {
    override suspend fun loadFuture() {
        locker.mutex.withLock {

            val loading = PageableState.Loading.Future(
                content = state.getState().content
            )
            state.setState(loading)
            runCatching {
                val res = futureLoader.loadFuture().throwIfHasError()
                res.throwIfHasError()
                entityConverter.convertAll(res.body()!!).asReversed()
            }.onFailure {
                val errorState = PageableState.Error(
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
                            PageableState.Fixed(
                                StateContent.Exist(
                                    newList
                                )
                            )
                        )
                    }
                    is StateContent.NotExist -> {
                        state.setState(
                            PageableState.Fixed(
                                StateContent.Exist(it)
                            )
                        )
                    }
                }
            }
        }
    }
}