package jp.panta.misskeyandroidclient.model.notes.draft

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import jp.panta.misskeyandroidclient.model.notes.draft.db.PollChoiceDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.UserIdDTO

abstract class DraftNoteDao {

    @Transaction
    fun fullInsert(draftNote: DraftNote): Long?{
        val id = insert(draftNote)
        val files = draftNote.draftFiles
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
    abstract fun insert(draftNote: DraftNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDraftFiles(list: List<DraftFile>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPollChoices(pollChoices: List<PollChoiceDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUserIds(userIds: List<UserIdDTO>)

    @Transaction
    fun findDraftNote(draftNoteId: Long): DraftNote{
        return findDraftNote(draftNoteId).apply{
            loadDraftNotesData(this)
        }
    }

    @Transaction
    fun findDraftNotesByAccountId(accountId: String): List<DraftNote>{
        return findDraftNotesByAccountId(accountId).map {
            it.apply{
                loadDraftNotesData(it)
            }
        }
    }

    private fun loadDraftNotesData(draftNote: DraftNote){
        draftNote.draftNoteId?.let{ id ->
            draftNote.visibleUserIds = findVisibleUserIdsByDraftNoteId(id)?.map{
                it.userId
            }
            draftNote.draftFiles = findDraftFilesByDraftNoteId(id)
            findPollChoicesByDraftNoteId(id)?.let{
                it.map{ choice ->
                    choice.choice
                }
            }?.let{
                draftNote.draftPoll?.choices = it
            }
        }
    }

    @Query("select * from draft_note where :accountId = accountId")
    abstract fun findAllFromAccountId(accountId: String): List<DraftNote>

    @Query("select * from draft_file where draft_note_id = :draftNoteId")
    abstract fun findDraftFilesByDraftNoteId(draftNoteId: Long): List<DraftFile>?

    @Query("select * from poll_choice where draft_note_id = :draftNoteId")
    abstract fun findPollChoicesByDraftNoteId(draftNoteId: Long): List<PollChoiceDTO>?

    @Query("select * from user_id where draft_note_id = :draftNoteId")
    abstract fun findVisibleUserIdsByDraftNoteId(draftNoteId: Long): List<UserIdDTO>?




}