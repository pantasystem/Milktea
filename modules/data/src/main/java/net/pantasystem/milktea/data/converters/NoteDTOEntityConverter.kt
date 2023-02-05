package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteVisibilityType
import net.pantasystem.milktea.api.misskey.notes.PollDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDTOEntityConverter @Inject constructor() {

    suspend fun convert(noteDTO: NoteDTO, account: Account, nodeInfo: NodeInfo?): Note {
        val visibility = Visibility(
            noteDTO.visibility ?: NoteVisibilityType.Public,
            isLocalOnly = noteDTO.localOnly ?: false,
            visibleUserIds = noteDTO.visibleUserIds?.map { id ->
                User.Id(account.accountId, id)
            } ?: emptyList()
        )
        return Note(
            id = Note.Id(account.accountId, noteDTO.id),
            createdAt = noteDTO.createdAt,
            text = noteDTO.text,
            cw = noteDTO.cw,
            userId = User.Id(account.accountId, noteDTO.userId),
            replyId = noteDTO.replyId?.let { Note.Id(account.accountId, noteDTO.replyId!!) },
            renoteId = noteDTO.renoteId?.let { Note.Id(account.accountId, noteDTO.renoteId!!) },
            viaMobile = noteDTO.viaMobile,
            visibility = visibility,
            localOnly = noteDTO.localOnly,
            emojis = (noteDTO.emojiList ?: emptyList()) + (noteDTO.reactionEmojis?.map {
                Emoji(name = it.key, uri = it.value, url = it.value)
            } ?: emptyList()),
            app = noteDTO.app?.toModel(),
            fileIds = noteDTO.fileIds?.map { FileProperty.Id(account.accountId, it) },
            poll = noteDTO.poll?.toPoll(),
            reactionCounts = noteDTO.reactionCounts?.map {
                ReactionCount(reaction = it.key, it.value)
            } ?: emptyList(),
            renoteCount = noteDTO.renoteCount,
            repliesCount = noteDTO.replyCount,
            uri = noteDTO.uri,
            url = noteDTO.url,
            visibleUserIds = noteDTO.visibleUserIds?.map {
                User.Id(account.accountId, it)
            } ?: emptyList(),
            myReaction = noteDTO.myReaction,
            channelId = noteDTO.channelId?.let {
                Channel.Id(account.accountId, it)
            },
            type = Note.Type.Misskey(
                channel = noteDTO.channel?.let {
                    Note.Type.Misskey.SimpleChannelInfo(
                        id = Channel.Id(account.accountId, it.id),
                        name = it.name
                    )
                }
            ),
            nodeInfo = nodeInfo,
        )
    }
}

fun PollDTO?.toPoll(): Poll? {
    return this?.let { dto ->
        Poll(
            multiple = dto.multiple,
            expiresAt = dto.expiresAt,
            choices = choices.mapIndexed { index, value ->
                Poll.Choice(
                    index = index,
                    text = value.text,
                    isVoted = value.isVoted,
                    votes = value.votes
                )
            }
        )
    }
}


@Throws(IllegalArgumentException::class)
fun Visibility(type: NoteVisibilityType, isLocalOnly: Boolean, visibleUserIds: List<User.Id>? = null): Visibility {
    return when(type){
        NoteVisibilityType.Public -> Visibility.Public(isLocalOnly)
        NoteVisibilityType.Followers -> Visibility.Followers(isLocalOnly)
        NoteVisibilityType.Home -> Visibility.Home(isLocalOnly)
        NoteVisibilityType.Specified -> Visibility.Specified(visibleUserIds ?: emptyList())
        else -> Visibility.Public(isLocalOnly)
    }
}
