package net.pantasystem.milktea.data.model.notes.draft.db

import androidx.room.*
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.channel.Channel
import net.pantasystem.milktea.data.model.notes.draft.DraftNote

@Entity(tableName = "draft_note_table", foreignKeys = [
    ForeignKey(
        parentColumns = ["accountId"],
        childColumns = ["accountId"],
        entity = Account::class,
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
],
indices = [Index("accountId", "text")])
data class DraftNoteDTO(
    val accountId: Long,
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
    val channelId: String? = null,
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
                draftNote.channelId?.channelId,
                DraftPollDTO.make(draftNote.draftPoll)
            ).apply {
                draftNoteId = draftNote.draftNoteId
            }
        }
    }

    @Ignore
    fun toDraftNote(
        accountId: Long,
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
                it.toFile(accountId)
            },
            viaMobile,
            localOnly,
            noExtractMentions,
            noExtractHashtags,
            noExtractEmojis,
            replyId,
            renoteId,
            poll?.toDraftPoll(pollChoicesDTO),
            channelId = channelId?.let {
                Channel.Id(accountId, it)
            }


        ).apply{
            draftNoteId = this@DraftNoteDTO.draftNoteId
        }
    }

}