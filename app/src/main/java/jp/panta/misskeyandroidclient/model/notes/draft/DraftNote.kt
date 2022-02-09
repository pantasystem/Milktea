package jp.panta.misskeyandroidclient.model.notes.draft

import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.getName
import jp.panta.misskeyandroidclient.model.notes.isLocalOnly
import java.io.Serializable
import java.util.*


data class DraftNote(
    val accountId: Long,
    val visibility: String = "public",
    var visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    var files: List<File>? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val draftPoll: DraftPoll? = null,
    val reservationPostingAt: Date? = null,

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
        draftPoll = this.note.poll?.toDraftPoll()
    )
}