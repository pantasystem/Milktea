package net.pantasystem.milktea.common.paginator

interface IdFutureLoader<Id, DTO> {
    suspend fun loadFuture(id: Id?): Result<List<DTO>>
}
