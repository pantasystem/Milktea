package net.pantasystem.milktea.data.infrastructure.instance

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.*
import java.net.URL
import javax.inject.Inject

class MetaRepositoryImpl @Inject constructor(
    private val metaDataSource: MetaDataSource,
    private val metaCache: MetaCache,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): MetaRepository {

    override suspend fun sync(instanceDomain: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val meta = fetch(instanceDomain)
            metaDataSource.add(meta)
            metaCache.put(meta.uri, meta)
            HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
        }
    }

    override suspend fun find(instanceDomain: String): Result<Meta> = runCancellableCatching {
        withContext(ioDispatcher) {
            val cacheMeta = metaCache.get(instanceDomain)
            if (cacheMeta != null) {
                return@withContext cacheMeta
            }
            val localMeta = metaDataSource.get(instanceDomain)
            if (localMeta == null) {
                val meta = fetch(instanceDomain)
                metaDataSource.add(meta)
            } else {
                localMeta
            }.also { meta ->
                metaCache.put(meta.uri, meta)
                HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
            }
        }

    }

    override fun get(instanceDomain: String): Meta? {
        return metaCache.get(instanceDomain)
    }

    override fun observe(instanceDomain: String): Flow<Meta?> {
        return metaDataSource.observe(instanceDomain).onEach { meta ->
            if (meta != null) {
                metaCache.put(instanceDomain, meta)
                HostWithVersion.put(URL(meta.uri).host, meta.getVersion())
            }
        }
    }

    private suspend fun fetch(instanceDomain: String): Meta {
        val res = misskeyAPIProvider.get(instanceDomain).getMeta(RequestMeta(detail = true))
            .throwIfHasError()
        val meta = res.body()!!
        return if (meta.emojis == null) {
            meta.copy(
                emojis = runCancellableCatching {
                    fetchEmojis(instanceDomain)?.map {
                        it.copy(
                            url = if (it.url == null) V13EmojiUrlResolver.resolve(it, instanceDomain) else it.url,
                            uri = if (it.uri == null) V13EmojiUrlResolver.resolve(it, instanceDomain) else it.uri,
                        )
                    }
                }.getOrNull()
            )
        } else {
            meta
        }
    }

    private suspend fun fetchEmojis(instanceDomain: String): List<Emoji>? {
        return misskeyAPIProvider.get(instanceDomain).getEmojis(EmptyRequest).throwIfHasError().body()?.emojis?.map {
            it.toModel()
        }
    }
}