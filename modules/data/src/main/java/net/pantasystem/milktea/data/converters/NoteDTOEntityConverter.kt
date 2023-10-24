package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteVisibilityType
import net.pantasystem.milktea.api.misskey.notes.PollDTO
import net.pantasystem.milktea.api.misskey.notes.ReactionAcceptanceType
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.Visibility
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteDTOEntityConverter @Inject constructor(
    private val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    private val imageCacheRepository: ImageCacheRepository,
    private val instanceInfoService: InstanceInfoService,
) {

    suspend fun convert(account: Account, noteDTO: NoteDTO): Note {
        val emojis = (noteDTO.emojiList + (noteDTO.reactionEmojiList))

        val instanceInfo = instanceInfoService.find(account.normalizedInstanceUri).getOrNull()
        val isRequireNyaize = (instanceInfo?.isRequirePerformNyaizeFrontend ?: false)
                && (noteDTO.user.isCat ?: false)
        val aspects = customEmojiAspectRatioDataSource.findIn(emojis.mapNotNull {
            it.url ?: it.uri
        }).getOrElse {
            emptyList()
        }.associate {
            it.uri to it.aspectRatio
        }
        val fileCaches = imageCacheRepository.findBySourceUrls(emojis.mapNotNull {
            it.url ?: it.uri
        }).associateBy {
            it.sourceUrl
        }
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
            emojis = emojis.map {
                it.toModel(aspects[it.url ?: it.uri], fileCaches[it.url ?: it.uri]?.cachePath)
            },
            app = null,
            fileIds = noteDTO.fileIds?.map { FileProperty.Id(account.accountId, it) },
            poll = noteDTO.poll?.toPoll(),
            reactionCounts = noteDTO.reactionCounts?.map {
                ReactionCount(
                    reaction = it.key,
                    count = it.value,
                    me = noteDTO.myReaction == it.key
                )
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
                },
                isAcceptingOnlyLikeReaction = when (noteDTO.reactionAcceptance) {
                    ReactionAcceptanceType.LikeOnly4Remote -> noteDTO.uri != null
                    ReactionAcceptanceType.LikeOnly -> true
                    ReactionAcceptanceType.NonSensitiveOnly -> false
                    ReactionAcceptanceType.NonSensitiveOnly4LocalOnly4Remote -> false
                    null -> false
                },
                isNotAcceptingSensitiveReaction = when (noteDTO.reactionAcceptance) {
                    ReactionAcceptanceType.NonSensitiveOnly -> true
                    ReactionAcceptanceType.NonSensitiveOnly4LocalOnly4Remote -> true
                    else -> false
                },
                isRequireNyaize = isRequireNyaize,
            ),
            maxReactionsPerAccount = 1
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
fun Visibility(
    type: NoteVisibilityType,
    isLocalOnly: Boolean,
    visibleUserIds: List<User.Id>? = null,
): Visibility {
    return when (type) {
        NoteVisibilityType.Public -> Visibility.Public(isLocalOnly)
        NoteVisibilityType.Followers -> Visibility.Followers(isLocalOnly)
        NoteVisibilityType.Home -> Visibility.Home(isLocalOnly)
        NoteVisibilityType.Specified -> Visibility.Specified(visibleUserIds ?: emptyList())
        else -> Visibility.Public(isLocalOnly)
    }
}
