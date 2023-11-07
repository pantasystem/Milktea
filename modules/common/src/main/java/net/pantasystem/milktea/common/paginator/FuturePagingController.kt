package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching


class FuturePagingController<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val futureLoader: FutureLoader<DTO>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FuturePaginator {

    companion object {
        fun <T, DTO, E> create(impl: T): FuturePagingController<DTO, E> where T : EntityConverter<DTO, E>, T : StateLocker, T : PaginationState<E>, T : FutureLoader<DTO> {
            return FuturePagingController(
                impl,
                impl,
                impl,
                impl
            )
        }
    }

    override suspend fun loadFuture(): Result<Int> {
        if (locker.mutex.isLocked) {
            return Result.failure(IllegalStateException("ローディング中にさらにローディング処理を実行することはできません"))
        }
        return locker.mutex.withLock {

            val loading = PageableState.Loading.Future(
                content = state.getState().content
            )
            state.setState(loading)
            runCancellableCatching {
                withContext(dispatcher) {
                    val res = futureLoader.loadFuture().getOrThrow()

                    // MisskeyではsinceIdで取得した場合に[1, 2, 3, 4, 5]という順番で取得される
                    // しかしアプリ上では[5, 4, 3, 2, 1]という順番で扱えた方が好ましいため、
                    // reverseするようにしている。
                    // ※ untilIdの場合は[5, 4, 3, 2, 1]という順番で取得される。
                    entityConverter.convertAll(res).asReversed()
                }
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
            }.map {
                it.size
            }
        }
    }
}