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
    override suspend fun loadFuture(): Result<Int> {
        if (locker.mutex.isLocked) {
            return Result.failure(IllegalStateException())
        }
        return locker.mutex.withLock {

            val loading = PageableState.Loading.Future(
                content = state.getState().content
            )
            state.setState(loading)
            runCancellableCatching {
                withContext(dispatcher) {
                    val res = futureLoader.loadFuture().getOrThrow()
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