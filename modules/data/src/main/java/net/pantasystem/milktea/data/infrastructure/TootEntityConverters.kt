package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.api.mastodon.poll.TootPollDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.user.User

fun TootPollDTO?.toPoll(): Poll? {
    return this?.let { dto ->
        Poll(
            multiple = dto.multiple,
            expiresAt = dto.expiresAt,
            choices = dto.options.mapIndexed { index, value ->
                Poll.Choice(
                    index = index,
                    text = value.title,
                    isVoted = (!dto.multiple && dto.voted == true) || dto.ownVotes?.firstOrNull {
                        it == index
                    } != null,
                    votes = dto.votesCount
                )
            }
        )
    }
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
        fileIds = emptyList(),
        poll = poll.toPoll(),
        type = Note.Type.Mastodon,
    )
}
