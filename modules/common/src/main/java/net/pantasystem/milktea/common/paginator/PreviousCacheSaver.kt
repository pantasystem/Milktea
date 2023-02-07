package net.pantasystem.milktea.common.paginator

interface PreviousCacheSaver<E> {
    suspend fun savePrevious(elements: List<E>)
}