package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.DefaultDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
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
    private val customEmojiInserter: CustomEmojiInserter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    memoryCacheCleaner: MemoryCacheCleaner,
) : CustomEmojiRepository {

    init {
        memoryCacheCleaner.register(customEmojiCache)
    }

    override suspend fun findBy(host: String): Result<List<CustomEmoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val emojisInMemory = customEmojiCache.get(host)
            if (emojisInMemory != null) {
                return@withContext emojisInMemory
            }

            val converted = loadAndConvert(host)

            customEmojiCache.put(host, converted)
            converted
        }
    }

    override suspend fun findByName(host: String, name: String): Result<List<CustomEmoji>> = runCancellableCatching {
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
            customEmojiInserter.convertAndReplaceAll(
                nodeInfo.host,
                remoteEmojis,
            )
        }
    }

    override fun observeBy(host: String, withAliases: Boolean): Flow<List<CustomEmoji>> {
        return customEmojiDAO.observeBy(host).map { list ->
            convertToModel(list)
        }.onEach {
            if (!withAliases) {
                customEmojiCache.put(host, it)
            }
        }.flowOn(ioDispatcher)
    }

    private suspend fun loadAndConvert(host: String): List<CustomEmoji> {
        val nodeInfo = nodeInfoRepository.find(host).getOrThrow()

        val emojis =  customEmojiDAO.findByHost(host).ifEmpty {
            val remoteEmojis = fetch(nodeInfo).getOrThrow()
            customEmojiInserter.convertAndReplaceAll(
                host,
                remoteEmojis,
            )
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

        return emojis.map {
            it.toModel(
                aspectRatio = aspects[it.url ?: it.uri]?.aspectRatio,
                cachePath = fileCaches[it.url ?: it.uri]?.cachePath,
            )
        }
    }

    private suspend fun fetch(nodeInfo: NodeInfo): Result<List<EmojiWithAlias>> = runCancellableCatching {
        customEmojiApiAdapter.fetch(nodeInfo)
    }

    override fun get(host: String): List<CustomEmoji>? {
        return customEmojiCache.get(host)
    }

    override suspend fun addEmojis(host: String, emojis: List<EmojiWithAlias>): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val ids = customEmojiDAO.insertAll(
                emojis.map {
                    CustomEmojiRecord.from(it.emoji, host)
                }
            )
            insertAliases(ids, emojis.map {
                it.aliases ?: emptyList()
            })
            // NOTE: inMemキャッシュなどを更新したい
            findBy(host).getOrThrow()
        }
    }

    override suspend fun deleteEmojis(host: String, emojis: List<CustomEmoji>): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            customEmojiDAO.deleteByHostAndNames(host, emojis.map { it.name })
            // NOTE: inMemキャッシュなどを更新したい
            findBy(host).getOrThrow()
        }
    }

    override fun getAndConvertToMap(host: String): Map<String, CustomEmoji>? {
        return customEmojiCache.getMap(host)
    }

    override suspend fun findAndConvertToMap(
        host: String
    ): Result<Map<String, CustomEmoji>> = runCancellableCatching{
        val cached = customEmojiCache.getMap(host)
        if (cached.isNullOrEmpty()) {

            val converted = withContext(ioDispatcher) {
                loadAndConvert(host)
            }
            customEmojiCache.put(host, converted)
        } else {
            cached
        }
    }

    override fun observeWithSearch(host: String, keyword: String): Flow<List<CustomEmoji>> {
        return customEmojiDAO.observeAndSearch(host, keyword).map { list ->
            convertToModel(list)
        }.flowOn(defaultDispatcher).onStart {
            customEmojiCache.get(host)?.filter {
                it.name.contains(keyword)
            }?.let {
                emit(it)
            }
        }
    }

    override suspend fun findByNames(host: String, names: List<String>): Result<List<CustomEmoji>>  = runCancellableCatching {
        withContext(ioDispatcher) {
            customEmojiDAO.findByNames(host, names).map {
                it.toModel()
            }
        }
    }

    override suspend fun search(host: String, keyword: String): Result<List<CustomEmoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            convertToModel(customEmojiDAO.search(host, keyword))
        }
    }

    private suspend fun convertToModel(emojis: List<CustomEmojiRecord>): List<CustomEmoji> {
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

    private suspend fun insertAliases(emojiIds: List<Long>, aliases: List<List<String>>) {
        assert(emojiIds.size == aliases.size) {
            "emojis.size != aliases.size"
        }
        emojiIds.asSequence().mapIndexed { index, l ->
            aliases[index].map {
                CustomEmojiAliasRecord(
                    name = it,
                    emojiId = l
                )
            }
        }.flatten().chunked(500).toList().map { chunkedAliases ->
            customEmojiDAO.insertAliases(chunkedAliases)
        }.flatten().toList()

    }
}

