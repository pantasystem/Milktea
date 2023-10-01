package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.DefaultDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.EmojiWithAlias
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import javax.inject.Inject

internal class CustomEmojiRepositoryImpl @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
    private val customEmojiApiAdapter: CustomEmojiApiAdapter,
    private val customEmojiCache: CustomEmojiCache,
    private val aspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    private val imageCacheRepository: ImageCacheRepository,
    private val customEmojiDAO: CustomEmojiDAO,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : CustomEmojiRepository {

    override suspend fun findBy(host: String, withAliases: Boolean): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val emojisInMemory = customEmojiCache.get(host)
            if (emojisInMemory != null) {
                return@withContext emojisInMemory
            }
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()

            val emojis =  customEmojiDAO.findByHost(host).ifEmpty {
                val remoteEmojis = fetch(nodeInfo).getOrThrow()
                customEmojiDAO.deleteByHost(nodeInfo.host)

                val emojisBeforeInsert = remoteEmojis.map {
                    CustomEmojiRecord.from(it.emoji, nodeInfo.host)
                }

                emojisBeforeInsert.chunked(500).map { chunkedEmojis ->
                    val ids = customEmojiDAO.insertAll(chunkedEmojis)
                    chunkedEmojis.mapIndexed { index, customEmojiRecord ->
                        customEmojiRecord.copy(id = ids[index])
                    }
                }.flatten()
                // TODO: aliasの同期
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

            val converted = emojis.map {
                it.toModel(
                    aspectRatio = aspects[it.url ?: it.uri]?.aspectRatio,
                    cachePath = fileCaches[it.url ?: it.uri]?.cachePath,
                )
            }

            // NOTE: aliasの含む絵文字はメモリ上にキャッシュしない
            if (!withAliases) {
                customEmojiCache.put(host, converted)
            }
            converted

        }
    }

    override suspend fun findByName(host: String, name: String): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val dtoList = customEmojiDAO.findByHostAndName(host, name)
            val aspects = aspectRatioDataSource.findIn(
                dtoList.mapNotNull {
                    it.url ?: it.uri
                }
            ).getOrElse { emptyList() }.associateBy {
                it.uri
            }
            val fileCaches = imageCacheRepository.findBySourceUrls(dtoList.mapNotNull {
                it.url ?: it.uri
            }).associateBy {
                it.sourceUrl
            }
            dtoList.map {
                it.toModel(
                    aspects[it.url ?: it.uri]?.aspectRatio,
                    fileCaches[it.url ?: it.uri]?.cachePath,
                )

            }
        }
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            val remoteEmojis = fetch(nodeInfo).getOrThrow()
            val aspects = aspectRatioDataSource.findIn(remoteEmojis.mapNotNull {
                it.emoji.url ?: it.emoji.uri
            }).getOrElse { emptyList() }.associateBy {
                it.uri
            }
            val fileCaches = imageCacheRepository.findBySourceUrls(remoteEmojis.mapNotNull {
                it.emoji.url ?: it.emoji.uri
            }).associateBy {
                it.sourceUrl
            }
            val emojis = remoteEmojis.map {
                it.emoji.copy(
                    aspectRatio = aspects[it.emoji.url ?: it.emoji.uri]?.aspectRatio ?: it.emoji.aspectRatio,
                    cachePath = fileCaches[it.emoji.url ?: it.emoji.uri]?.cachePath,
                )
            }

            customEmojiCache.put(host, emojis)
            customEmojiDAO.deleteByHost(nodeInfo.host)

            remoteEmojis.map {
                CustomEmojiRecord.from(it.emoji, nodeInfo.host)
            }.chunked(500).map { chunkedEmojis ->
                customEmojiDAO.insertAll(chunkedEmojis)
            }.flatten()

            // TODO: aliasの同期
        }
    }

    override fun observeBy(host: String, withAliases: Boolean): Flow<List<Emoji>> {
        return customEmojiDAO.observeBy(host).map { list ->
            convertToModel(list)
        }.onEach {
            if (!withAliases) {
                customEmojiCache.put(host, it)
            }
        }.flowOn(ioDispatcher)
    }

    private suspend fun fetch(nodeInfo: NodeInfo): Result<List<EmojiWithAlias>> = runCancellableCatching {
        customEmojiApiAdapter.fetch(nodeInfo)
    }

    override fun get(host: String): List<Emoji>? {
        return customEmojiCache.get(host)
    }

    override suspend fun addEmojis(host: String, emojis: List<Emoji>): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            customEmojiDAO.insertAll(
                emojis.map {
                    CustomEmojiRecord.from(it, host)
                }
            )
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

    override fun observeWithSearch(host: String, keyword: String): Flow<List<Emoji>> {
        return customEmojiDAO.search(host, keyword).map { list ->
            convertToModel(list)
        }.flowOn(defaultDispatcher)
    }


    private suspend fun convertToModel(emojis: List<CustomEmojiRecord>): List<Emoji> {
        val aspects = aspectRatioDataSource.findIn(
            emojis.mapNotNull {
                it.url ?: it.uri
            }
        ).getOrElse { emptyList() }.associateBy {
            it.uri
        }
        val fileCaches = imageCacheRepository.findBySourceUrls(emojis.mapNotNull {
            it.url ?: it.uri
        }).associateBy {
            it.sourceUrl
        }
        return emojis.map {
            it.toModel(
                aspects[it.url ?: it.uri]?.aspectRatio,
                fileCaches[it.url ?: it.uri]?.cachePath,
            )
        }
    }
}

