package net.pantasystem.milktea.api.Instance.remote

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.RequestMeta

class RemoteFetchMeta(
    val misskeyAPIProvider: MisskeyAPIProvider
) : FetchMeta {

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun fetch(instanceDomain: String, isForceFetch: Boolean): Meta {
        return misskeyAPIProvider.get(instanceDomain)
            .getMeta(RequestMeta())
            .throwIfHasError()
            .body() ?: throw IllegalStateException("metaの取得に失敗")
    }
}