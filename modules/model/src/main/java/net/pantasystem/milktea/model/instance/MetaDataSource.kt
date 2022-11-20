package net.pantasystem.milktea.model.instance

import kotlinx.coroutines.flow.Flow


interface MetaDataSource {

    suspend fun add(meta: Meta) : Meta

    suspend fun get(instanceDomain: String) : Meta?

    suspend fun delete(meta: Meta)

    fun observe(instanceDomain: String) : Flow<Meta?>
}