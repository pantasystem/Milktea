package net.pantasystem.milktea.data.infrastructure.emoji

import net.pantasystem.milktea.api.misskey.EmptyRequest
import net.pantasystem.milktea.api.misskey.instance.RequestMeta
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.emoji.V13EmojiUrlResolver
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.emoji.EmojiWithAlias
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.getVersion
import javax.inject.Inject


internal interface CustomEmojiApiAdapter {
    suspend fun fetch(nodeInfo: NodeInfo): List<EmojiWithAlias>
}

internal class CustomEmojiApiAdapterImpl @Inject constructor(
    val mastodonAPIProvider: MastodonAPIProvider,
    val misskeyAPIProvider: MisskeyAPIProvider
) : CustomEmojiApiAdapter {

    override suspend fun fetch(nodeInfo: NodeInfo): List<EmojiWithAlias> {
        return when (nodeInfo.type) {
            is NodeInfo.SoftwareType.Mastodon -> {
                val emojis = mastodonAPIProvider.get("https://${nodeInfo.host}").getCustomEmojis()
                    .throwIfHasError()
                    .body()
                emojis?.map {
                    it.toEmojiWithAlias()
                }
            }
            is NodeInfo.SoftwareType.Pleroma -> {
                val emojis = mastodonAPIProvider.get(nodeInfo.host).getCustomEmojis()
                    .throwIfHasError()
                    .body()
                emojis?.map {
                    it.toEmojiWithAlias()
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
                        it.toModelWithAlias()
                    }?.map {
                        it.copy(
                            emoji = it.emoji.copy(
                                url = if (it.emoji.url == null) V13EmojiUrlResolver.resolve(it.emoji, "https://${nodeInfo.host}") else it.emoji.url,
                                uri = if (it.emoji.uri == null) V13EmojiUrlResolver.resolve(it.emoji, "https://${nodeInfo.host}") else it.emoji.uri,
                            )
                        )
                    }
                } else {
                    misskeyAPIProvider.get("https://${nodeInfo.host}")
                        .getMeta(RequestMeta(detail = true))
                        .throwIfHasError()
                        .body()
                        ?.emojis?.map {
                            it.toModelWithAlias()
                        }
                }
            }
            is NodeInfo.SoftwareType.Firefish -> {
                misskeyAPIProvider.get("https://${nodeInfo.host}")
                    .getMeta(RequestMeta(detail = true))
                    .throwIfHasError()
                    .body()
                    ?.emojis?.map {
                        it.toModelWithAlias()
                    }
            }
            is NodeInfo.SoftwareType.Other -> throw IllegalStateException()
        } ?: throw IllegalArgumentException()
    }
}