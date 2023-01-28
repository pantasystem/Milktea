package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiAliasRecord
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.db.toRecord
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.RequestMeta
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import javax.inject.Inject

class CustomEmojiRepositoryImpl @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
    private val customEmojiDAO: CustomEmojiDAO,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
): CustomEmojiRepository {

    override suspend fun findBy(host: String): Result<List<Emoji>> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            var emojis = customEmojiDAO.findBy(host).map {
                it.toModel()
            }
            if (emojis.isEmpty()) {
                emojis = fetch(nodeInfo).getOrThrow()
                upInsert(nodeInfo.host, emojis)
            }
            emojis
        }
    }

    override suspend fun sync(host: String): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            val nodeInfo = nodeInfoRepository.find(host).getOrThrow()
            val emojis = fetch(nodeInfo).getOrThrow()
            customEmojiDAO.deleteByHost(nodeInfo.host)
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
        }.flowOn(ioDispatcher)
    }

    private suspend fun fetch(nodeInfo: NodeInfo): Result<List<Emoji>> = runCancellableCatching {
        when(nodeInfo.type) {
            is NodeInfo.SoftwareType.Mastodon -> {
                val emojis = mastodonAPIProvider.get("https://${nodeInfo.host}").getCustomEmojis()
                    .throwIfHasError()
                    .body()
                emojis?.map {
                    it.toEmoji()
                }
            }
            is NodeInfo.SoftwareType.Misskey -> {
                if (nodeInfo.type.getVersion() >= Version("13")) {
                    val emojis = misskeyAPIProvider.get("https://${nodeInfo.host}").getEmojis(EmptyRequest)
                        .throwIfHasError()
                        .body()
                    emojis?.emojis?.map {
                        it.copy(
                            url = V13EmojiUrlResolver.resolve(it, "https://${nodeInfo.host}"),
                            uri = V13EmojiUrlResolver.resolve(it, "https://${nodeInfo.host}"),
                        )
                    }
                } else {
                    misskeyAPIProvider.get("https://${nodeInfo.host}").getMeta(RequestMeta(detail = true))
                        .throwIfHasError()
                        .body()
                        ?.emojis
                }
            }
            is NodeInfo.SoftwareType.Other -> throw IllegalStateException()
        } ?: throw IllegalArgumentException()
    }

    private suspend fun upInsert(host: String, emojis: List<Emoji>) {
        val record = emojis.map {
            it.toRecord(host)
        }
        val ids = customEmojiDAO.insertAll(record)

        ids.mapIndexed { index, id ->
            if (id == -1L) {
                customEmojiDAO.findBy(host, emojis[index].name)?.let { record ->
                    customEmojiDAO.update(emojis[index].toRecord(host, record.emoji.id))
                    customEmojiDAO.deleteAliasByEmojiId(record.emoji.id)
                    emojis[index].aliases?.map {
                        CustomEmojiAliasRecord(
                            emojiId = record.emoji.id,
                            it
                        )
                    }?.let {
                        customEmojiDAO.insertAliases(it)
                    }
                    record.emoji.id
                }
            } else {
                id
            }
        }
    }

}