package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.toPoll
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.instance.MastodonInstanceInfoRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TootDTOEntityConverter @Inject constructor(
    private val instanceInfoRepository: MastodonInstanceInfoRepository,
    private val loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("TootDTOEntityConverter")
    }

    suspend fun convert(statusDTO: TootStatusDTO, account: Account, nodeInfo: NodeInfo?): Note {
        val isReactionAvailable = (instanceInfoRepository.find(account.normalizedInstanceDomain)
            .onFailure {
                logger.error("Failed to find instance info", it)
            }
            .getOrNull()?.isReactionAvailable
            ?: false) || nodeInfo?.type is NodeInfo.SoftwareType.Mastodon.Fedibird
        return with(statusDTO) {
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
                emojis = emojis.map {
                    it.toEmoji()
                } + (emojiReactions?.mapNotNull {
                    it.getEmoji()
                } ?: emptyList()),
                app = null,
                reactionCounts = emojiReactions?.map {
                    ReactionCount(
                        reaction = it.reaction,
                        count = it.count
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
                nodeInfo = nodeInfo,
            )
        }

    }
}