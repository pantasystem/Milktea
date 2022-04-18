package net.pantasystem.milktea.data.model.instance.db

import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryMetaRepository : MetaRepository {

    private val instanceDomainAndMeta = MutableStateFlow(emptyMap<String, Meta>())
    private val lock = Mutex()

    override suspend fun add(meta: Meta): Meta {
        lock.withLock {
            instanceDomainAndMeta.value = instanceDomainAndMeta.value.toMutableMap().also {
                it[meta.uri] = meta
            }
            return meta
        }


    }

    override suspend fun delete(meta: Meta) {
        lock.withLock {
            instanceDomainAndMeta.value = instanceDomainAndMeta.value.toMutableMap().also {
                it.remove(meta.uri)
            }
        }
    }

    override suspend fun get(instanceDomain: String): Meta? {
        lock.withLock {
            return instanceDomainAndMeta.value[instanceDomain]
        }
    }

    override fun observe(instanceDomain: String): Flow<Meta?> {
        return instanceDomainAndMeta.map {
            it[instanceDomain]
        }
    }
}