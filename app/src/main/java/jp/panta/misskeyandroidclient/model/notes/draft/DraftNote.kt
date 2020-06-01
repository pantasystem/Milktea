package jp.panta.misskeyandroidclient.model.notes.draft

import androidx.room.*
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll

@Entity(tableName = "draft_note")
data class DraftNote(
    val accountId: String,
    val visibility: String = "public",
    @Ignore var visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    @Ignore var draftFiles: List<DraftFile>? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    @Embedded val draftPoll: DraftPoll?


){

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "draft_note_id") var draftNoteId: Long? = null

}