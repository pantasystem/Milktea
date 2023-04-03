package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.delegate.CustomEmojiUpInsertDelegate
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import javax.inject.Inject

internal class CustomEmojiRepositoryImpl @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
    private val customEmojiDAO: CustomEmojiDAO,
    private val customEmojiApiAdapter: CustomEmojiApiAdapter,
    private val customEmojiCache: CustomEmojiCache,
    private val upInsert: CustomEmojiUpInsertDelegate,
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
            customEmojiCache.put(host, emojis)
            emojis
        }
    }

    override suspend fun findByName(host: String, name: String): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            customEmojiDAO.findBy(host, name).map {
                it.toModel()
            }
        }
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            val emojis = fetch(nodeInfo).getOrThrow()
            customEmojiDAO.deleteByHost(nodeInfo.host)
            customEmojiCache.put(host, emojis)
            upInsert(nodeInfo.host, emojis)
        }
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeBy(host: String): Flow<List<Emoji>> {
        return suspend {
            nodeInfoRepository.find(host).getOrThrow()
        }.asFlow().flatMapLatest {
            customEmojiDAO.observeBy(host).map { list ->
                list.map {
                    it.toModel()
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

