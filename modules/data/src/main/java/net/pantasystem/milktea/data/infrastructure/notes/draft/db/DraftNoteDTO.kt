package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.data.infrastructure.account.db.AccountRecord
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.ReactionAcceptanceType
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteFile
import java.util.Date

@Entity(
    tableName = "draft_note_table", foreignKeys = [
        ForeignKey(
            parentColumns = ["accountId"],
            childColumns = ["accountId"],
            entity = AccountRecord::class,
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId", "text")]
)
data class DraftNoteDTO(
    @ColumnInfo(name = "accountId")
    val accountId: Long,

    @ColumnInfo(name = "visibility")
    val visibility: String = "public",

    @ColumnInfo(name = "text")
    val text: String?,

    @ColumnInfo(name = "cw")
    val cw: String? = null,

    @ColumnInfo(name = "viaMobile")
    val viaMobile: Boolean? = null,

    @ColumnInfo(name = "localOnly")
    val localOnly: Boolean? = null,

    @ColumnInfo(name = "noExtractMentions")
    val noExtractMentions: Boolean? = null,

    @ColumnInfo(name = "noExtractHashtags")
    val noExtractHashtags: Boolean? = null,

    @ColumnInfo(name = "noExtractEmojis")
    val noExtractEmojis: Boolean? = null,

    @ColumnInfo(name = "replyId")
    val replyId: String? = null,

    @ColumnInfo(name = "renoteId")
    val renoteId: String? = null,

    @ColumnInfo(name = "channelId")
    val channelId: String? = null,

    @ColumnInfo(name = "scheduleWillPostAt")
    val scheduleWillPostAt: Date? = null,

    @Embedded val poll: DraftPollDTO?,

    @ColumnInfo(name = "draft_note_id")
    @PrimaryKey(autoGenerate = true)
    var draftNoteId: Long? = null,

    @ColumnInfo(name = "isSensitive")
    val isSensitive: Boolean? = null,

    @ColumnInfo(name = "reactionAcceptanceType")
    val reactionAcceptanceType: String? = null,
) {



    companion object {
        fun make(draftNote: DraftNote): DraftNoteDTO {
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
                draftNote.reservationPostingAt,
                DraftPollDTO.make(draftNote.draftPoll),
                draftNoteId = if(draftNote.draftNoteId == 0L) null else draftNote.draftNoteId,
                isSensitive = draftNote.isSensitive,
                reactionAcceptanceType = draftNote.reactionAcceptanceType?.name,
            )
        }
    }

    @Ignore
    fun toDraftNote(
        accountId: Long,
        visibilityUserIds: List<UserIdDTO>?,
        pollChoicesDTO: List<PollChoiceDTO>?,
        draftFiles: List<DraftNoteFile>?,
    ): DraftNote {
        return DraftNote(
            accountId,
            visibility,
            visibilityUserIds?.map {
                it.userId
            },
            text,
            cw,
            draftFiles,
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
            },
            draftNoteId = draftNoteId ?: 0L,
            reservationPostingAt = scheduleWillPostAt,
            isSensitive = isSensitive,
            reactionAcceptanceType = reactionAcceptanceType?.let { type ->
                ReactionAcceptanceType.values().find {
                    it.name == type
                }
            },
        )
    }

}