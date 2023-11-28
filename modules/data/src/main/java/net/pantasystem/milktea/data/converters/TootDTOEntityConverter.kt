package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.toPoll
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.image.ImageCache
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.instance.MastodonInstanceInfoRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TootDTOEntityConverter @Inject constructor(
    private val instanceInfoRepository: MastodonInstanceInfoRepository,
    private val nodeInfoRepository: NodeInfoRepository,
    private val imageCacheRepository: ImageCacheRepository,
    private val loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("TootDTOEntityConverter")
    }

    suspend fun convertAll(account: Account, statusDTOs: List<TootStatusDTO>): List<Note> {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val instanceInfo = instanceInfoRepository.find(account.normalizedInstanceUri)
        val isReactionAvailable = (instanceInfo.onFailure {
            logger.error("Failed to find instance info", it)
        }.getOrNull()?.isReactionAvailable ?: false) || nodeInfo?.type is NodeInfo.SoftwareType.Mastodon.Fedibird

        val urls = statusDTOs.flatMap { statusDTO ->
            (statusDTO.emojiReactions?.mapNotNull {
                it.url
            }?: emptyList()) + (statusDTO.emojiReactions?.mapNotNull {
                it.url
            }?: emptyList())
        }
        val imageCaches = imageCacheRepository.findBySourceUrls(urls).associateBy {
            it.sourceUrl
        }
        return statusDTOs.map {
            convert(account, it, instanceInfo, isReactionAvailable, imageCaches)
        }
    }

    suspend fun convert(statusDTO: TootStatusDTO, account: Account): Note {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val instanceInfoResult = instanceInfoRepository.find(account.normalizedInstanceUri)
        val isReactionAvailable = (instanceInfoResult
            .onFailure {
                logger.error("Failed to find instance info", it)
            }
            .getOrNull()?.isReactionAvailable
            ?: false) || nodeInfo?.type is NodeInfo.SoftwareType.Mastodon.Fedibird

        val urls = (statusDTO.emojiReactions?.mapNotNull {
            it.url
        }?: emptyList()) + (statusDTO.emojiReactions?.mapNotNull {
            it.url
        }?: emptyList())
        val imageCaches = imageCacheRepository.findBySourceUrls(urls).associateBy {
            it.sourceUrl
        }
        return convert(
            account = account,
            statusDTO = statusDTO,
            instanceInfoResult = instanceInfoResult,
            isReactionAvailable = isReactionAvailable,
            imageCaches = imageCaches,
        )
    }

    private fun convert(
        account: Account,
        statusDTO: TootStatusDTO,
        instanceInfoResult: Result<MastodonInstanceInfo>,
        isReactionAvailable: Boolean,
        imageCaches: Map<String, ImageCache>,
    ): Note {
        return with(statusDTO) {
            val emojis = emojis.map {
                it.toEmoji(imageCaches[it.url]?.cachePath)
            } + (emojiReactions?.mapNotNull {
                it.getEmoji(imageCaches[it.url]?.cachePath)
            } ?: emptyList())
            Note(
                id = Note.Id(account.accountId, id),
                text = this.content,
                cw = this.spoilerText.takeIf {
                    it.isNotBlank()
                },
                userId = User.Id(account.accountId, this.account.id),
                replyId = this.inReplyToId?.let { Note.Id(account.accountId, this.inReplyToId!!) },
                renoteId = this.quote?.id?.let { Note.Id(account.accountId, it) }
                    ?: this.reblog?.id?.let { Note.Id(account.accountId, this.reblog?.id!!) },
                viaMobile = null,
                visibility = net.pantasystem.milktea.data.infrastructure.Visibility(
                    visibility,
                    circleId,
                    visibilityEx
                ),
                localOnly = null,
                emojis = emojis,
                app = null,
                reactionCounts = emojiReactions?.map {
                    ReactionCount(
                        reaction = it.reaction,
                        count = it.count,
                        me = it.me ?: false,
                    )
                } ?: emptyList(),
                repliesCount = repliesCount,
                renoteCount = reblogsCount,
                uri = this.uri,
                url = this.url,
                visibleUserIds = emptyList(),
                myReaction = emojiReactions?.firstOrNull {
                    it.myReaction() != null
                }?.myReaction(),
                channelId = null,
                createdAt = createdAt,
                fileIds = mediaAttachments.map {
                    FileProperty.Id(account.accountId, it.id)
                },
                poll = poll.toPoll(),
                maxReactionsPerAccount = instanceInfoResult.getOrNull()?.maxReactionsPerAccount ?: 0,
                type = Note.Type.Mastodon(
                    favorited = favourited,
                    reblogged = reblogged,
                    bookmarked = bookmarked,
                    muted = muted,
                    favoriteCount = favouritesCount,
                    tags = tags?.map {
                        it.toModel()
                    } ?: emptyList(),
                    mentions = mentions?.map {
                        it.toModel()
                    } ?: emptyList(),
                    isFedibirdQuote = quote != null,
                    pollId = poll?.id,
                    isSensitive = sensitive,
                    pureText = text,
                    isReactionAvailable = isReactionAvailable
                ),
                emojiNameMap = emojis.associateBy { it.name }
            )
        }
    }
}