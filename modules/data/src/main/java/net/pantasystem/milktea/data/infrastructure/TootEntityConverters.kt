package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.mastodon.accounts.MastodonAccountRelationshipDTO
import net.pantasystem.milktea.api.mastodon.media.TootMediaAttachment
import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
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
                    isVoted = (!(dto.multiple ?: false) && dto.voted == true) || dto.ownVotes?.firstOrNull {
                        it == index
                    } != null,
                    votes = dto.votesCount ?: 0
                )
            }
        )
    }
}

fun TootMediaAttachment.toFileProperty(account: Account): FileProperty {
    return FileProperty(
        id = FileProperty.Id(account.accountId, id),
        name = "",
        createdAt = null,
        type = type,
        md5 = null,
        size = null,
        url = url,
        thumbnailUrl = previewUrl,
        blurhash = blurhash,
        comment = description,
    )
}

fun TootStatusDTO.toNote(account: Account): Note {
    return Note(
        id = Note.Id(account.accountId, id),
        text = this.content,
        cw = this.spoilerText.takeIf {
            sensitive
        },
        userId = User.Id(account.accountId, this.account.id),
        replyId = this.inReplyToId?.let{ Note.Id(account.accountId, this.inReplyToId!!) },
        renoteId = this.reblog?.id?.let{ Note.Id(account.accountId, this.reblog?.id!!) },
        viaMobile = null,

        // TODO: 正しいVisibilityを得るようにする
        visibility = Visibility.Public(false),
        localOnly = null,
        emojis = emojis.map {
            it.toEmoji()
        },
        app = null,
        reactionCounts = emptyList(),
        repliesCount = repliesCount,
        renoteCount = reblogsCount,
        uri = this.uri,
        url = this.url,
        visibleUserIds = emptyList(),
        myReaction = null,
        channelId = null,
        createdAt = createdAt,
        fileIds = mediaAttachments.map {
            FileProperty.Id(account.accountId, it.id)
        },
        poll = poll.toPoll(),
        type = Note.Type.Mastodon,
    )
}

fun TootStatusDTO.toEntities(account: Account): NoteRelationEntities {
    val users = mutableListOf<User>()
    val notes = mutableListOf<Note>()
    val files = mutableListOf<FileProperty>()
    pickEntities(account, notes, users, files)
    return NoteRelationEntities(
        note = toNote(account),
        files = files,
        users = users,
        notes = notes,
    )
}

fun TootStatusDTO.pickEntities(account: Account, notes: MutableList<Note>, users: MutableList<User>, files: MutableList<FileProperty>) {
    val (note, user) = toNote(account) to this.account.toModel(account)
    notes.add(note)
    users.add(user)
    files.addAll(
        mediaAttachments.map {
            it.toFileProperty(account)
        }
    )
    this.reblog?.pickEntities(account, notes, users, files)
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