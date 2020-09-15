package jp.panta.misskeyandroidclient.model.instance.db

import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryMetaRepository : MetaRepository{

    val instanceDomainAndMeta = ConcurrentHashMap<String, Meta>()

    override suspend fun add(meta: Meta): Meta {

        instanceDomainAndMeta[meta.uri] = meta
        return meta

    }

    override suspend fun delete(meta: Meta) {
        instanceDomainAndMeta.remove(meta.uri)
    }

    override suspend fun get(instanceDomain: String): Meta? {
        return instanceDomainAndMeta[instanceDomain]
    }
}