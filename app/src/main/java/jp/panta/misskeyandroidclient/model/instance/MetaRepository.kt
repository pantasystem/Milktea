package jp.panta.misskeyandroidclient.model.instance

interface MetaRepository {

    suspend fun add(meta: Meta) : Meta

    suspend fun get(instanceDomain: String) : Meta?

    suspend fun delete(meta: Meta)
}