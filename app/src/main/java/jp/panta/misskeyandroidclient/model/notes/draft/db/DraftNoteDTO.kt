package jp.panta.misskeyandroidclient.model.notes.draft.db

import androidx.room.*
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.notes.draft.DraftFile
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll

@Entity(tableName = "draft_note", foreignKeys = [
    ForeignKey(
        parentColumns = ["id"],
        childColumns = ["accountId"],
        entity = Account::class,
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
],
indices = [Index("accountId", "text")])
data class DraftNoteDTO(
    val accountId: String,
    val visibility: String = "public",
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    @Embedded val poll: DraftPollDTO?

){

    @ColumnInfo(name="draft_note_id")
    @PrimaryKey(autoGenerate = true)
    var draftNoteId: Long? = null

    companion object{
        fun make(draftNote: DraftNote): DraftNoteDTO{
            return DraftNoteDTO(
                draftNote.accountId,
                draftNote.visibility,
                draftNote.text,
                draftNote.cw,
                draftNote.viaMobile,
                draftNote.localOnly,
                draftNote.noExtractMentions,
                draftNote.noExtractHashtags,
                draftNote.noExtractEmojis,
                draftNote.replyId,
                draftNote.renoteId,
                DraftPollDTO.make(draftNote.draftPoll)
            ).apply {
                draftNoteId = draftNote.draftNoteId
            }
        }
    }

    @Ignore
    fun toDraftNote(
        visibilityUserIds: List<UserIdDTO>?,
        draftFiles: List<DraftFileDTO>?,
        pollChoicesDTO: List<PollChoiceDTO>?
    ): DraftNote{
        return DraftNote(
            accountId,
            visibility,
            visibilityUserIds?.map{
                it.userId
            },
            text,
            cw,
            draftFiles?.map{
                it.toDraftFile()
            },
            viaMobile,
            localOnly,
            noExtractMentions,
            noExtractHashtags,
            noExtractEmojis,
            replyId,
            renoteId,
            poll?.toDraftPoll(pollChoicesDTO)


        ).apply{
            draftNoteId = this@DraftNoteDTO.draftNoteId
        }
    }

}