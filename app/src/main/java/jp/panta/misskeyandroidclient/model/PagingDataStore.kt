package jp.panta.misskeyandroidclient.model

interface PagingDataStore<K, T> {

    data class Page<K>(val id: K)
    fun loadNew(id: Page<K>): List<T>?

    fun loadOld(id: Page<K>): List<T>?

    fun loadInit(id: Page<K>): List<T>?

}