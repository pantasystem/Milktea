package net.pantasystem.milktea.common.paginator

import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import kotlinx.coroutines.sync.withLock

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
                val res = previousLoader.loadPrevious().getOrThrow()
                entityConverter.convertAll(res)
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
