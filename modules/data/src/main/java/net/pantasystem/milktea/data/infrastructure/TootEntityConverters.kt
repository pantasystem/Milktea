package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.api.mastodon.status.StatusVisibilityType
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.user.User

fun TootPollDTO?.toPoll(): Poll? {
    return this?.let { dto ->
        Poll(
            multiple = dto.multiple ?: false,
            expiresAt = dto.expiresAt,
            choices = dto.options.mapIndexed { index, value ->
                Poll.Choice(
                    index = index,
                    text = value.title,
                    isVoted = (!(dto.multiple
                        ?: false) && dto.voted == true) || dto.ownVotes?.firstOrNull {
                        it == index
                    } != null,
                    votes = dto.votesCount ?: 0
                )
            }
        )
    }
}

fun TootMediaAttachment.toFileProperty(account: Account, isSensitive: Boolean): FileProperty {
    return FileProperty(
        id = FileProperty.Id(account.accountId, id),
        name = "",
        createdAt = null,
        type = type,
        md5 = null,
        size = null,
        url = url ?: "",
        isSensitive = isSensitive,
        thumbnailUrl = previewUrl,
        blurhash = blurhash,
        comment = description,
    )
}

fun TootStatusDTO.toNote(account: Account, nodeInfo: NodeInfo?): Note {
    return Note(
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
        visibility = Visibility(visibility, circleId, visibilityEx),
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
            pureText = text
        ),
        nodeInfo = nodeInfo,
    )
}

fun TootStatusDTO.toEntities(account: Account, nodeInfo: NodeInfo?): NoteRelationEntities {
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()
    pickEntities(account, notes, users, files, nodeInfo)
    return NoteRelationEntities(
        note = toNote(account, nodeInfo),
        files = files,
        users = users,
        notes = notes,
    )
}

fun TootStatusDTO.pickEntities(
    account: Account,
    notes: MutableList<Note>,
    users: MutableList<User>,
    files: MutableList<FileProperty>,
    nodeInfo: NodeInfo?
) {
    val (note, user) = toNote(account, nodeInfo) to this.account.toModel(account)
    notes.add(note)
    users.add(user)
    files.addAll(
        mediaAttachments.map {
            it.toFileProperty(account, sensitive)
        }
    )
    this.reblog?.pickEntities(account, notes, users, files, nodeInfo)
    this.quote?.pickEntities(account, notes, users, files, nodeInfo)
}

fun MastodonAccountRelationshipDTO.toUserRelated(): User.Related {
    return User.Related(
        isFollower = followedBy,
        isFollowing = following,
        isBlocking = blocking,
        hasPendingFollowRequestFromYou = requested,
        isMuting = muting,
        hasPendingFollowRequestToYou = false,
    )
}

fun MstNotificationDTO.toModel(a: Account, isRead: Boolean): Notification {
    val id = Notification.Id(a.accountId, id)
    val userId = User.Id(a.accountId, account.id)
    return when(type) {
        MstNotificationDTO.NotificationType.Mention -> {
            MentionNotification(
                createdAt = createdAt,
                id = id,
                userId = userId,
                isRead = isRead,
                noteId = Note.Id(a.accountId, requireNotNull(status).id)
            )
        }
        MstNotificationDTO.NotificationType.Status -> {
            StatusNotification(
                createdAt = createdAt,
                id = id,
                userId = userId,
                isRead = isRead,
                noteId = Note.Id(a.accountId, requireNotNull(status).id)
            )
        }
        MstNotificationDTO.NotificationType.Reblog -> {
            RenoteNotification(
                id = id,
                createdAt = createdAt,
                userId = userId,
                noteId = Note.Id(a.accountId, requireNotNull(status).id),
                isRead = isRead,
            )
        }
        MstNotificationDTO.NotificationType.Follow -> {
            FollowNotification(
                id = id,
                createdAt = createdAt,
                userId = userId,
                isRead = isRead,
            )
        }
        MstNotificationDTO.NotificationType.FollowRequest -> {
            FollowRequestAcceptedNotification(
                id = id,
                createdAt = createdAt,
                userId = userId,
                isRead = isRead,
            )
        }
        MstNotificationDTO.NotificationType.Favourite -> {
            FavoriteNotification(
                createdAt = createdAt,
                id = id,
                userId = userId,
                isRead = isRead,
                noteId = Note.Id(a.accountId, requireNotNull(status).id)
            )
        }
        MstNotificationDTO.NotificationType.Poll -> {
            PollEndedNotification(
                id = id,
                createdAt = createdAt,
                isRead = isRead,
                noteId = Note.Id(a.accountId, requireNotNull(status).id),
            )
        }
        MstNotificationDTO.NotificationType.Update -> {
            TODO("通知種別${type}はまだ実装されていません")
        }
        MstNotificationDTO.NotificationType.AdminSingUp -> {
            TODO("通知種別${type}はまだ実装されていません")
        }
        MstNotificationDTO.NotificationType.AdminReport -> {
            TODO("通知種別${type}はまだ実装されていません")
        }
        MstNotificationDTO.NotificationType.EmojiReaction -> {
            ReactionNotification(
                id = id,
                createdAt = createdAt,
                isRead = isRead,
                noteId = Note.Id(a.accountId, requireNotNull(status).id),
                reaction = requireNotNull(emojiReaction).reaction,
                userId = userId,
            )
        }
    }
}
fun Visibility(type: StatusVisibilityType, circleId: String? = null, visibilityEx: String? = null,): Visibility {
    return when(type) {
        StatusVisibilityType.Private -> {
            when(visibilityEx) {
                "limited" -> Visibility.Limited(circleId)
                else -> Visibility.Followers(false)
            }
        }
        StatusVisibilityType.Unlisted -> Visibility.Home(false)
        StatusVisibilityType.Public -> Visibility.Public(false)
        StatusVisibilityType.Direct -> when(visibilityEx) {
            "personal" -> Visibility.Personal
            else -> Visibility.Specified(emptyList())
        }
    }
}

fun Instance.toModel(): MastodonInstanceInfo {
    return MastodonInstanceInfo(
        uri = uri,
        title = title,
        description = description,
        email = email,
        urls = urls.let {
            MastodonInstanceInfo.Urls(
                streamingApi = it.streamingApi
            )
        },
        version = version,
        configuration = configuration?.let { config ->
            MastodonInstanceInfo.Configuration(
                statuses = config.statuses?.let {
                    MastodonInstanceInfo.Configuration.Statuses(
                        maxCharacters = it.maxCharacters,
                        maxMediaAttachments = it.maxMediaAttachments,
                    )
                },
                polls = config.polls?.let {
                    MastodonInstanceInfo.Configuration.Polls(
                        maxOptions = it.maxOptions,
                        maxCharactersPerOption = it.maxCharactersPerOption,
                        maxExpiration = it.maxExpiration,
                        minExpiration = it.minExpiration,
                    )
                }
            )
        },
        fedibirdCapabilities = fedibirdCapabilities,
    )
}