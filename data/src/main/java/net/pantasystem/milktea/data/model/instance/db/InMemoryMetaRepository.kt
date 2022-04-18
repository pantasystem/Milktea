package net.pantasystem.milktea.api.Instance.db

import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryMetaRepository : net.pantasystem.milktea.model.instance.MetaRepository {

    private val instanceDomainAndMeta = MutableStateFlow(emptyMap<String, net.pantasystem.milktea.model.instance.Meta>())
    private val lock = Mutex()

    override suspend fun add(meta: net.pantasystem.milktea.model.instance.Meta): net.pantasystem.milktea.model.instance.Meta {
        lock.withLock {
            instanceDomainAndMeta.value = instanceDomainAndMeta.value.toMutableMap().also {
                it[meta.uri] = meta
            }
            return meta
        }


    }

    override suspend fun delete(meta: net.pantasystem.milktea.model.instance.Meta) {
        lock.withLock {
            instanceDomainAndMeta.value = instanceDomainAndMeta.value.toMutableMap().also {
                it.remove(meta.uri)
            }
        }
    }

    override suspend fun get(instanceDomain: String): net.pantasystem.milktea.model.instance.Meta? {
        lock.withLock {
            return instanceDomainAndMeta.value[instanceDomain]
        }
    }

    override fun observe(instanceDomain: String): Flow<net.pantasystem.milktea.model.instance.Meta?> {
        return instanceDomainAndMeta.map {
            it[instanceDomain]
        }
    }
}