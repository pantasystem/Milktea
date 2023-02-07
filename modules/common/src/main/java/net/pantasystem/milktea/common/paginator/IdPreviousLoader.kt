package net.pantasystem.milktea.common.paginator

import net.pantasystem.milktea.common.PageableState

interface IdPreviousLoader<Id, DTO, E> {
    suspend fun loadPrevious(state: PageableState<List<E>>, id: Id?): Result<List<DTO>>
}
