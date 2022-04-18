package net.pantasystem.milktea.model.notes.draft

import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.getName
import net.pantasystem.milktea.model.notes.isLocalOnly
import java.io.Serializable
import java.util.*


data class DraftNote(
    val accountId: Long,
    val visibility: String = "public",
    var visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    var files: List<net.pantasystem.milktea.model.file.File>? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val draftPoll: DraftPoll? = null,
    val reservationPostingAt: Date? = null,
    val channelId: net.pantasystem.milktea.model.channel.Channel.Id? = null,

    ): Serializable{

    var draftNoteId: Long? = null

}

fun NoteRelation.toDraftNote() : DraftNote {
    return DraftNote(
        accountId = this.note.id.accountId,
        visibility = this.note.visibility.getName(),
        visibleUserIds = this.note.visibleUserIds?.map {
            it.id
        },
        text = this.note.text,
        cw = this.note.cw,
        files = this.files?.map {
            it.toFile()
        },
        viaMobile = this.note.viaMobile,
        localOnly = this.note.visibility.isLocalOnly(),
        renoteId = this.note.renoteId.let {
            it?.noteId
        },
        replyId = this.note.replyId.let {
            it?.noteId
        },
        draftPoll = this.note.poll?.toDraftPoll(),
        channelId = this.note.channelId,
    )
}