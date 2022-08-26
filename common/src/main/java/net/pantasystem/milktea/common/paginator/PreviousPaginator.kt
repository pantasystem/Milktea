package net.pantasystem.milktea.common.paginator

interface PreviousPaginator {
    suspend fun loadPrevious(): Result<Int>
}
