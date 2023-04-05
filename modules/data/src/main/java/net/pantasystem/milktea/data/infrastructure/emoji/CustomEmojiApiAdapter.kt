package net.pantasystem.milktea.data.infrastructure.emoji

import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.instance.RequestMeta
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.getVersion
import javax.inject.Inject


internal interface CustomEmojiApiAdapter {
    suspend fun fetch(nodeInfo: NodeInfo): List<Emoji>
}

internal class CustomEmojiApiAdapterImpl @Inject constructor(
    val mastodonAPIProvider: MastodonAPIProvider,
    val misskeyAPIProvider: MisskeyAPIProvider
) : CustomEmojiApiAdapter {

    override suspend fun fetch(nodeInfo: NodeInfo): List<Emoji> {
        return when (nodeInfo.type) {
            is NodeInfo.SoftwareType.Mastodon -> {
                val emojis = mastodonAPIProvider.get("https://${nodeInfo.host}").getCustomEmojis()
                    .throwIfHasError()
                    .body()
                emojis?.map {
                    it.toEmoji()
                }
            }
            is NodeInfo.SoftwareType.Pleroma -> {
                val emojis = mastodonAPIProvider.get("https://${nodeInfo.host}").getCustomEmojis()
                    .throwIfHasError()
                    .body()
                emojis?.map {
                    it.toEmoji()
                }
            }
            is NodeInfo.SoftwareType.Misskey -> {
                if (
                    nodeInfo.type.getVersion() >= Version("13")
                    && nodeInfo.type !is NodeInfo.SoftwareType.Misskey.Calckey
                ) {
                    val emojis =
                        misskeyAPIProvider.get("https://${nodeInfo.host}").getEmojis(EmptyRequest)
                            .throwIfHasError()
                            .body()
                    emojis?.emojis?.map {
                        it.copy(
                            url = if (it.url == null) V13EmojiUrlResolver.resolve(it, "https://${nodeInfo.host}") else it.url,
                            uri = if (it.uri == null) V13EmojiUrlResolver.resolve(it, "https://${nodeInfo.host}") else it.uri,
                        )
                    }
                } else {
                    misskeyAPIProvider.get("https://${nodeInfo.host}")
                        .getMeta(RequestMeta(detail = true))
                        .throwIfHasError()
                        .body()
                        ?.emojis
                }
            }
            is NodeInfo.SoftwareType.Other -> throw IllegalStateException()
        } ?: throw IllegalArgumentException()
    }
}