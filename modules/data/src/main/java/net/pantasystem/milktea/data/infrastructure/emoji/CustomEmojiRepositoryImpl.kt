package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.delegate.CustomEmojiUpInsertDelegate
import net.pantasystem.milktea.data.infrastructure.emoji.objectbox.ObjectBoxCustomEmojiRecordDAO
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import javax.inject.Inject

internal class CustomEmojiRepositoryImpl @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
    private val customEmojiDAO: CustomEmojiDAO,
    private val customEmojiApiAdapter: CustomEmojiApiAdapter,
    private val customEmojiCache: CustomEmojiCache,
    private val upInsert: CustomEmojiUpInsertDelegate,
    private val aspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    private val imageCacheRepository: ImageCacheRepository,
    private val objectBoxCustomEmojiDao: ObjectBoxCustomEmojiRecordDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : CustomEmojiRepository {

    override suspend fun findBy(host: String): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            var emojis = customEmojiCache.get(host)
            if (emojis != null) {
                return@withContext emojis
            }
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            emojis = customEmojiDAO.findBy(host).map {
                it.toModel()
            }

            if (emojis.isEmpty()) {
                emojis = fetch(nodeInfo).getOrThrow()
                upInsert(nodeInfo.host, emojis)
            }
            val aspects = aspectRatioDataSource.findIn(emojis.mapNotNull {
                it.url ?: it.uri
            }).getOrElse { emptyList() }.associateBy {
                it.uri
            }
            val fileCaches = imageCacheRepository.findBySourceUrls(emojis.mapNotNull {
                it.url ?: it.uri
            }).associateBy {
                it.sourceUrl
            }

            emojis = emojis.map {
                it.copy(
                    aspectRatio = aspects[it.url ?: it.uri]?.aspectRatio ?: it.aspectRatio,
                    cachePath = fileCaches[it.url ?: it.uri]?.cachePath,
                )
            }
            customEmojiCache.put(host, emojis)
            emojis
        }
    }

    override suspend fun findByName(host: String, name: String): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val dtoList = customEmojiDAO.findBy(host, name)
            val aspects = aspectRatioDataSource.findIn(
                dtoList.mapNotNull {
                    it.emoji.url ?: it.emoji.uri
                }
            ).getOrElse { emptyList() }.associateBy {
                it.uri
            }
            val fileCaches = imageCacheRepository.findBySourceUrls(dtoList.mapNotNull {
                it.emoji.url ?: it.emoji.uri
            }).associateBy {
                it.sourceUrl
            }
            dtoList.map {
                it.toModel(
                    aspects[it.emoji.url ?: it.emoji.uri]?.aspectRatio,
                    fileCaches[it.emoji.url ?: it.emoji.uri]?.cachePath,
                )

            }
        }
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            var emojis = fetch(nodeInfo).getOrThrow()
            val aspects = aspectRatioDataSource.findIn(emojis.mapNotNull {
                it.url ?: it.uri
            }).getOrElse { emptyList() }.associateBy {
                it.uri
            }
            val fileCaches = imageCacheRepository.findBySourceUrls(emojis.mapNotNull {
                it.url ?: it.uri
            }).associateBy {
                it.sourceUrl
            }
            emojis = emojis.map {
                it.copy(
                    aspectRatio = aspects[it.url ?: it.uri]?.aspectRatio ?: it.aspectRatio,
                    cachePath = fileCaches[it.url ?: it.uri]?.cachePath,
                )
            }

            customEmojiCache.put(host, emojis)
            customEmojiDAO.deleteByHost(host)
            upInsert(nodeInfo.host, emojis)
        }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeBy(host: String): Flow<List<Emoji>> {
        return suspend {
            nodeInfoRepository.find(host).getOrThrow()
        }.asFlow().flatMapLatest {
            customEmojiDAO.observeBy(host).map { list ->
                val aspects = aspectRatioDataSource.findIn(
                    list.mapNotNull {
                        it.emoji.url ?: it.emoji.uri
                    }
                ).getOrElse { emptyList() }.associateBy {
                    it.uri
                }
                val fileCaches = imageCacheRepository.findBySourceUrls(list.mapNotNull {
                    it.emoji.url ?: it.emoji.uri
                }).associateBy {
                    it.sourceUrl
                }
                list.map {
                    it.toModel(
                        aspects[it.emoji.url ?: it.emoji.uri]?.aspectRatio,
                        fileCaches[it.emoji.url ?: it.emoji.uri]?.cachePath,
                    )
                }
            }
        }.onEach {
            customEmojiCache.put(host, it)
        }.flowOn(ioDispatcher)
    }

    private suspend fun fetch(nodeInfo: NodeInfo): Result<List<Emoji>> = runCancellableCatching {
        customEmojiApiAdapter.fetch(nodeInfo)
    }

    override fun get(host: String): List<Emoji>? {
        return customEmojiCache.get(host)
    }

    override suspend fun addEmojis(host: String, emojis: List<Emoji>): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            upInsert(host, emojis)
            // NOTE: inMemキャッシュなどを更新したい
            findBy(host).getOrThrow()
        }
    }

    override suspend fun deleteEmojis(host: String, emojis: List<Emoji>): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            customEmojiDAO.deleteByHostAndNames(host, emojis.map { it.name })
            // NOTE: inMemキャッシュなどを更新したい
            findBy(host).getOrThrow()
        }
    }

    override fun getAndConvertToMap(host: String): Map<String, Emoji>? {
        return customEmojiCache.getMap(host)
    }

}

