package net.pantasystem.milktea.common.paginator

import net.pantasystem.milktea.common.PageableState

interface IdFutureLoader<Id, DTO, E> {
    suspend fun loadFuture(state: PageableState<List<E>>, id: Id?): Result<List<DTO>>
}
