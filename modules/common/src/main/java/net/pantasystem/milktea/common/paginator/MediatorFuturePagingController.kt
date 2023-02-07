package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching

class MediatorFuturePagingController<Id, DTO, Record, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val localRecordConverter: EntityConverter<Record, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val futureLoader: IdFutureLoader<Id, DTO>,
    private val localFutureLoader: IdFutureLoader<Id, Record>,
    private val idGetter: IdGetter<Id>,
    private val futureCacheSaver: FutureCacheSaver<DTO>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FuturePaginator {

    override suspend fun loadFuture(): Result<Int> {
        if (locker.mutex.isLocked) {
            return Result.failure(IllegalStateException())
        }
        return locker.mutex.withLock {
            runCancellableCatching {
                val beforeUpdateState = state.getState()
                val id = idGetter.getUntilId()
                val loading = PageableState.Loading.Future(
                    content = state.getState().content
                )
                state.setState(loading)
                val localCaches = withContext(dispatcher) {
                    localFutureLoader.loadFuture(id)
                }.mapCancellableCatching {
                    localRecordConverter.convertAll(it)
                }.getOrThrow()

                applyLocalSourceState(beforeUpdateState, localCaches)

                val futureRawRes = withContext(dispatcher) {
                    futureLoader.loadFuture(id)
                }
                val remoteRes = futureRawRes.mapCancellableCatching {
                    entityConverter.convertAll(it)
                }.getOrThrow()

                futureCacheSaver.saveFuture(futureRawRes.getOrThrow())

                applyFinalState(beforeUpdateState, remoteRes)

                remoteRes.size
            }

        }
    }

    private fun applyLocalSourceState(state: PageableState<List<E>>, it: List<E>) {
        when(val content = state.content) {
            is StateContent.Exist -> {
                val newList = content.rawContent.toMutableList()
                newList.addAll(0, it)
                this.state.setState(
                    PageableState.Loading.Future(
                        StateContent.Exist(
                            newList
                        )
                    )
                )
            }
            is StateContent.NotExist -> {
                this.state.setState(
                    PageableState.Loading.Future(
                        StateContent.Exist(it)
                    )
                )
            }
        }
    }

    private fun applyFinalState(state: PageableState<List<E>>, it: List<E>) {
        when(val content = state.content) {
            is StateContent.Exist -> {
                val newList = content.rawContent.toMutableList()
                newList.addAll(0, it)
                this.state.setState(
                    PageableState.Fixed(
                        StateContent.Exist(
                            newList
                        )
                    )
                )
            }
            is StateContent.NotExist -> {
                this.state.setState(
                    PageableState.Fixed(
                        StateContent.Exist(it)
                    )
                )
            }
        }
    }
}