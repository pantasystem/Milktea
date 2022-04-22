package net.pantasystem.milktea.common.paginator

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState

interface PaginationState<T> {
    val state: Flow<PageableState<List<T>>>
    fun setState(state: PageableState<List<T>>)
    fun getState(): PageableState<List<T>>
}
