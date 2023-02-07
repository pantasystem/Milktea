package net.pantasystem.milktea.common.paginator

interface IdPreviousLoader<Id, DTO> {
    suspend fun loadPrevious(id: Id?): Result<List<DTO>>
}
