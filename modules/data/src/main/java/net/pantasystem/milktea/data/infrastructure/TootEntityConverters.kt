package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.api.mastodon.status.StatusVisibilityType
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.data.converters.TootDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
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


suspend fun TootStatusDTO.toEntities(
    converter: TootDTOEntityConverter,
    account: Account,
): NoteRelationEntities {
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()
    pickEntities(converter, account, notes, users, files)
    return NoteRelationEntities(
        note = converter.convert(this, account),
        files = files,
        users = users,
        notes = notes,
    )
}

suspend fun TootStatusDTO.pickEntities(
    converter: TootDTOEntityConverter,
    account: Account,
    notes: MutableList<Note>,
    users: MutableList<User>,
    files: MutableList<FileProperty>,
) {
    val (note, user) = converter.convert(this, account) to this.account.toModel(account)
    notes.add(note)
    users.add(user)
    files.addAll(
        mediaAttachments.map {
            it.toFileProperty(account, sensitive)
        }
    )
    this.reblog?.pickEntities(converter, account, notes, users, files)
    this.quote?.pickEntities(converter, account, notes, users, files)
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
                },
                emojiReactions = config.emojiReactions?.let {
                    MastodonInstanceInfo.Configuration.EmojiReactions(
                        maxReactions = it.maxReactions,
                        maxReactionsPerAccount = it.maxReactionsPerAccount,
                    )
                }
            )
        },
        fedibirdCapabilities = fedibirdCapabilities,
    )
}