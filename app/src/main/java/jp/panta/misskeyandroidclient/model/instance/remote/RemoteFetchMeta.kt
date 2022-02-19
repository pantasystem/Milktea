package jp.panta.misskeyandroidclient.model.instance.remote

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.FetchMeta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta

class RemoteFetchMeta(
    val misskeyAPIProvider: MisskeyAPIProvider
) : FetchMeta{

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean): Meta {
        return misskeyAPIProvider.get(instanceDomain)
            .getMeta(RequestMeta())
            .throwIfHasError()
            .body() ?: throw IllegalStateException("metaの取得に失敗")
    }
}