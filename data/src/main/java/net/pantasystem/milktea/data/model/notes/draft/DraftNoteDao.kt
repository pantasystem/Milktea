package net.pantasystem.milktea.data.model.notes.draft

import androidx.room.*
import net.pantasystem.milktea.data.model.notes.draft.db.*

@Dao
abstract class DraftNoteDao {

    fun fullInsert(draftNote: DraftNote): Long{
        val draftNoteDTO = DraftNoteDTO.make(draftNote)
        val id = insert(draftNoteDTO)
        val files = draftNote.files?.map{
            DraftFileDTO.make(it, id)
        }
        val pollChoices = draftNote.draftPoll?.choices?.let{
            it.mapIndexed { index, s ->
                PollChoiceDTO(
                    choice = s,
                    draftNoteId = id,
                    weight = index
                )
            }
        }
        val visibleUserIdDTOList = draftNote.visibleUserIds?.map{
            UserIdDTO(draftNoteId = id, userId = it)
        }

        if(!files.isNullOrEmpty()){
            insertDraftFiles(files)
        }
        if(!pollChoices.isNullOrEmpty()){
            insertPollChoices(pollChoices)
        }
        if(!visibleUserIdDTOList.isNullOrEmpty()){
            insertUserIds(visibleUserIdDTOList)
        }

        return id
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(draftNote: DraftNoteDTO): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDraftFiles(list: List<DraftFileDTO>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPollChoices(pollChoices: List<PollChoiceDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUserIds(userIds: List<UserIdDTO>)

    fun findDraftNotesByAccount(accountId: Long): List<DraftNote>{
        return findDraftNotesRelation(accountId).map{
            it.toDraftNote(accountId)
        }
    }


    fun getDraftNote(accountId: Long, draftNoteId: Long): DraftNote?{
        return getDraftNoteRelation(accountId, draftNoteId)?.toDraftNote(accountId)
    }

    fun deleteDraftNote(draftNote: DraftNote){
        draftNote.draftNoteId?.let{
            deleteDraftNote(draftNote.accountId, it)
        }
    }

    @Transaction
    @Query("select * from draft_note_table where accountId = :accountId")
    abstract fun findDraftNotesRelation(accountId: Long): List<DraftNoteRelation>

    @Transaction
    @Query("select * from draft_note_table where accountId = :accountId and text like '%'||:word||'%'")
    abstract fun searchByWordDraftNotesRelation(accountId: Long, word: String): List<DraftNoteRelation>

    @Transaction
    @Query("select * from draft_note_table where accountId = :accountId and draft_note_id = :draftNoteId")
    abstract fun getDraftNoteRelation(accountId: Long, draftNoteId: Long): DraftNoteRelation?

    @Transaction
    @Query("delete from 'draft_note_table' where accountId = :accountId and draft_note_id = :draftNoteId")
    abstract fun deleteDraftNote(accountId: Long, draftNoteId: Long)


    @Query("delete from 'draft_file_table' where draft_note_id = :draftNoteId and file_id = :fileId")
    abstract fun deleteFile(draftNoteId: Long, fileId: Long)

}