package jp.panta.misskeyandroidclient.model.instance

import kotlinx.coroutines.flow.Flow


interface MetaRepository {

    suspend fun add(meta: Meta) : Meta

    suspend fun get(instanceDomain: String) : Meta?

    suspend fun delete(meta: Meta)

    fun observe(instanceDomain: String) : Flow<Meta?>
}