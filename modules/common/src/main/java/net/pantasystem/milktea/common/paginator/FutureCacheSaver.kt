package net.pantasystem.milktea.common.paginator

interface FutureCacheSaver<E> {
    suspend fun saveFuture(elements: List<E>)
}