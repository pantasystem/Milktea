package net.pantasystem.milktea.model.note


import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.poll.CreatePoll
import net.pantasystem.milktea.model.user.User


/**
 * @param noExtractEmojis 本文からカスタム絵文字を展開しないか否か
 * @param noExtractMentions 本文からメンションを展開しないか否か
 * @param noExtractHashtags 本文からハッシュタグを展開しないか否か
 */
data class CreateNote(
    val author: Account,
    val visibility: Visibility,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var files: List<AppFile>? = null,
    val replyId: Note.Id? = null,
    val renoteId: Note.Id? = null,
    val poll: CreatePoll? = null,
    val draftNoteId: Long? = null,
    val channelId: Channel.Id? = null,
    val scheduleWillPostAt: Instant? = null,
    val isSensitive: Boolean? = null,
    val reactionAcceptance: ReactionAcceptanceType? = null,
)


fun DraftNote.toCreateNote(author: Account): CreateNote {
    return CreateNote(
        text = this.text,
        cw = this.cw,
        draftNoteId = this.draftNoteId,
        visibility = Visibility(
            type = this.visibility,
            isLocalOnly = this.localOnly ?: false,
            visibleUserIds = this.visibleUserIds?.map {
                User.Id(accountId = accountId, id = it)
            }),
        viaMobile = this.viaMobile ?: true,
        poll = draftPoll?.let {
            CreatePoll(
                choices = it.choices,
                multiple = it.multiple,
                expiresAt = it.expiresAt
            )
        },
        replyId = this.replyId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        renoteId = this.renoteId?.let {
            Note.Id(accountId = accountId, noteId = it)
        },
        files = draftFiles?.map {
            AppFile.from(it)
        } ?: emptyList(),
        scheduleWillPostAt = reservationPostingAt?.let {
            Instant.fromEpochMilliseconds(it.time)
        },
        channelId = channelId,
        author = author,
        isSensitive = isSensitive,
        reactionAcceptance = reactionAcceptanceType,
    )
}