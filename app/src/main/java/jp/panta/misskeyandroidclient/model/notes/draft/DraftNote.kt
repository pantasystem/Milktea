package jp.panta.misskeyandroidclient.model.notes.draft

import jp.panta.misskeyandroidclient.model.file.File
import java.io.Serializable


data class DraftNote(
    val accountId: String,
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
    val draftPoll: DraftPoll? = null


): Serializable{

    var draftNoteId: Long? = null

}