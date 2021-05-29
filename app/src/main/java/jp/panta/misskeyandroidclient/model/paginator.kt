package jp.panta.misskeyandroidclient.model

import kotlinx.coroutines.sync.Mutex

/**
 * ページネーションするためのIdを取得するためのインターフェース
 */
interface IdGetter<T> {
    suspend fun getUntilId(): T?
    suspend fun getSinceId(): T?
}

/**
 * 状態をロックすることのできることを表すインターフェース
 */
interface StateLocker {
    val mutex: Mutex
}

interface PageableState<T> {
    fun addAll(list: List<T>)
    fun addAllToFirst(list: List<T>)
    fun clear()
}

interface FuturePaginator {
    suspend fun loadFuture()
}

interface PreviousPaginator {
    suspend fun loadPrevious()
}