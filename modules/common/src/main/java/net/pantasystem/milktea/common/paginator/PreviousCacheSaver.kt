package net.pantasystem.milktea.common.paginator

interface PreviousCacheSaver<Id, E> {
    suspend fun savePrevious(key: Id?, elements: List<E>)
}