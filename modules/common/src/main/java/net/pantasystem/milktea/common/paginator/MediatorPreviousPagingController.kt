package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching

class MediatorPreviousPagingController<Id, DTO, Record, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val localRecordConverter: EntityConverter<Record, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val previousLoader: IdPreviousLoader<Id, DTO, E>,
    private val localPreviousLoader: IdPreviousLoader<Id, Record, E>,
    private val idGetter: IdGetter<Id>,
    private val previousCacheSaver: PreviousCacheSaver<Id, DTO>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PreviousPaginator {

    override suspend fun loadPrevious(): Result<Int> {
        if (locker.mutex.isLocked) {
            return Result.failure(IllegalStateException())
        }
        return locker.mutex.withLock {
            runCancellableCatching {
                val beforeUpdateState = state.getState()
                val id = idGetter.getUntilId()
                val loading = PageableState.Loading.Previous(
                    content = state.getState().content
                )
                state.setState(loading)
                val localCaches = withContext(dispatcher) {
                    localPreviousLoader.loadPrevious(beforeUpdateState, id)
                }.mapCancellableCatching {
                    localRecordConverter.convertAll(it)
                }.getOrThrow()

                applyLocalSourceState(beforeUpdateState, localCaches)

                val remoteRawRes = withContext(dispatcher) {
                    previousLoader.loadPrevious(beforeUpdateState, id)
                }
                val remoteRes = remoteRawRes.mapCancellableCatching {
                    entityConverter.convertAll(it)
                }.getOrThrow()

                withContext(dispatcher) {
                    previousCacheSaver.savePrevious(id, remoteRawRes.getOrThrow())
                }

                applyFinalState(beforeUpdateState, remoteRes)

                remoteRes.size
            }

        }
    }

    private fun applyLocalSourceState(state: PageableState<List<E>>, it: List<E>) {
        when(val content = state.content) {
            is StateContent.Exist -> {
                val newList = content.rawContent.toMutableList()
                newList.addAll(it)
                this.state.setState(
                    PageableState.Loading.Previous(
                        StateContent.Exist(
                            newList
                        )
                    )
                )
            }
            is StateContent.NotExist -> {
                this.state.setState(
                    PageableState.Loading.Previous(
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
                newList.addAll(it)
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