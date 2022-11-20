package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaDataSource
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.RequestMeta
import javax.inject.Inject

class MetaRepositoryImpl @Inject constructor(
    private val metaDataSource: MetaDataSource,
    private val metaCache: MetaCache,
    private val misskeyAPIProvider: MisskeyAPIProvider,
): MetaRepository {

    override suspend fun sync(instanceDomain: String): Result<Unit> = runCatching {
        val meta = fetch(instanceDomain)
        metaDataSource.add(meta)
        metaCache.put(instanceDomain, meta)
    }

    override suspend fun find(instanceDomain: String): Result<Meta> = runCatching {
        val cacheMeta = metaCache.get(instanceDomain)
        if (cacheMeta != null) {
            return@runCatching cacheMeta
        }
        val localMeta = metaDataSource.get(instanceDomain)
        if (localMeta == null) {
            val meta = fetch(instanceDomain)
            metaCache.put(instanceDomain, meta)
            metaDataSource.add(meta)
        } else {
            metaCache.put(instanceDomain, localMeta)
            localMeta
        }
    }

    override fun get(instanceDomain: String): Meta? {
        return metaCache.get(instanceDomain)
    }

    override fun observe(instanceDomain: String): Flow<Meta?> {
        return metaDataSource.observe(instanceDomain)
    }

    private suspend fun fetch(instanceDomain: String): Meta {
        val res = misskeyAPIProvider.get(instanceDomain).getMeta(RequestMeta(detail = true))
            .throwIfHasError()
        return res.body()!!
    }
}