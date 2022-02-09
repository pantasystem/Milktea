package jp.panta.misskeyandroidclient.model.instance.remote

import jp.panta.misskeyandroidclient.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaStore

class RemoteMetaStore : MetaStore{

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean): Meta {
        return MisskeyGetMeta.getMeta(instanceDomain)
            .execute()
            .throwIfHasError()
            .body()?: throw IllegalStateException("metaの取得に失敗")
    }
}