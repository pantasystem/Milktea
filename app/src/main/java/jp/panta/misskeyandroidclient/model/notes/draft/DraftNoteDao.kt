package jp.panta.misskeyandroidclient.model.notes.draft

import androidx.room.*
import jp.panta.misskeyandroidclient.model.notes.draft.db.*

@Dao
abstract class DraftNoteDao {

    fun fullInsert(draftNote: DraftNote): Long?{
        val draftNoteDTO = DraftNoteDTO.make(draftNote)
        val id = insert(draftNoteDTO)
        val files = draftNote.draftFiles?.map{
            DraftFileDTO.make(it)
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

    fun findDraftNotesByAccount(accountId: String): List<DraftNote>{
        return findDraftNotesRelation(accountId).map{
            it.draftNoteDTO.toDraftNote(it.visibilityUserIds, it.draftFiles, it.pollChoices)
        }
    }

    @Query("select * from draft_note where accountId = :accountId")
    abstract fun findDraftNotesRelation(accountId: String): List<DraftNoteRelation>







}