package net.pantasystem.milktea.data.model.instance.remote

import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.model.instance.Meta
import net.pantasystem.milktea.data.model.instance.FetchMeta
import net.pantasystem.milktea.data.model.instance.RequestMeta

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