package net.pantasystem.milktea.common.paginator

interface FuturePaginator {
    suspend fun loadFuture(): Result<Int>
}