package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class InMemoryMetaRepository : MetaRepository{

    private val instanceDomainAndMeta = ConcurrentHashMap<String, Meta>()
    private val lock = Mutex()

    override suspend fun add(meta: Meta): Meta {
        lock.withLock {
            instanceDomainAndMeta[meta.uri] = meta
            return meta
        }


    }

    override suspend fun delete(meta: Meta) {
        lock.withLock {
            instanceDomainAndMeta.remove(meta.uri)
        }
    }

    override suspend fun get(instanceDomain: String): Meta? {
        lock.withLock {
            return instanceDomainAndMeta[instanceDomain]
        }
    }
}