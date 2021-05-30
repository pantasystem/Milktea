package jp.panta.misskeyandroidclient.model.instance.remote

import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaStore

class RemoteMetaStore : MetaStore{

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun get(instanceDomain: String): Meta? {
        return MisskeyGetMeta.getMeta(instanceDomain).execute().body()
    }
}