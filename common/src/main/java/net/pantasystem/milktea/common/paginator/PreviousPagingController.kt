package net.pantasystem.milktea.common.paginator

import android.util.Log
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent

class PreviousPagingController<DTO, E>(
    private val entityConverter: EntityConverter<DTO, E>,
    private val locker: StateLocker,
    private val state: PaginationState<E>,
    private val previousLoader: PreviousLoader<DTO>
) : PreviousPaginator {
    override suspend fun loadPrevious() {
        if (locker.mutex.isLocked) {
            return
        }
        locker.mutex.withLock {

            val loading = PageableState.Loading.Previous(
                content = state.getState().content
            )
            state.setState(loading)
            runCatching {
                val res = previousLoader.loadPrevious().getOrThrow()
                entityConverter.convertAll(res)
            }.onFailure {
                Log.i("PreviousPagingCt", "load error", it)
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
