package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching

class PreviousPagingController<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val previousLoader: PreviousLoader<DTO>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PreviousPaginator {
    override suspend fun loadPrevious(): Result<Int> {
        if (locker.mutex.isLocked) {
            return Result.failure(IllegalStateException())
        }
        val result = locker.mutex.withLock {

            val loading = PageableState.Loading.Previous(
                content = state.getState().content
            )
            state.setState(loading)
            runCancellableCatching {
                withContext(dispatcher) {
                    val res = previousLoader.loadPrevious().getOrThrow()
                    entityConverter.convertAll(res)
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
        return result.map {
            it.size
        }
    }
}
