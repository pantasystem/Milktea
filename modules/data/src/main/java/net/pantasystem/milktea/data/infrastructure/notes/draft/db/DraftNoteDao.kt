package net.pantasystem.milktea.data.infrastructure.notes.draft.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.model.note.draft.DraftNote

@Dao
abstract class DraftNoteDao {

//    fun fullInsert(draftNote: DraftNote): Long{
//        val draftNoteDTO = DraftNoteDTO.make(draftNote)
//        val id = insert(draftNoteDTO)
//        val localFiles = draftNote.files?.mapNotNull {
//            it as DraftNoteFile.Local?
//        }?.filter {
//            it.draftNoteFileId == 0L
//        }?: emptyList()
//        insertLocalFiles(localFiles.map {
//            DraftLocalFile(
//                name = it.name,
//                filePath = it.filePath,
//                isSensitive = it.isSensitive,
//                folderId = it.folderId,
//                thumbnailUrl = it.thumbnailUrl,
//                type = it.type,
//            )
//        })
//
//        val remoteFiles = draftNote.files?.mapNotNull {
//            it as DraftNoteFile.Remote?
//        }?.filter {
//            it.draftNoteFileId == 0L
//        }?: emptyList()
//
//
//        insertDriveFiles(remoteFiles.map {
//            DriveFileRecord.from(it.fileProperty)
//        })
//
//
//        val files = draftNote.files?.map{
//            when(it) {
//                is DraftNoteFile.Local -> TODO()
//                is DraftNoteFile.Remote -> TODO()
//            }
//        }
//        val pollChoices = draftNote.draftPoll?.choices?.let{
//            it.mapIndexed { index, s ->
//                PollChoiceDTO(
//                    choice = s,
//                    draftNoteId = id,
//                    weight = index
//                )
//            }
//        }
//        val visibleUserIdDTOList = draftNote.visibleUserIds?.map{
//            UserIdDTO(draftNoteId = id, userId = it)
//        }
//
//        if(!files.isNullOrEmpty()){
//            insertDraftFiles(files)
//        }
//        if(!pollChoices.isNullOrEmpty()){
//            insertPollChoices(pollChoices)
//        }
//        if(!visibleUserIdDTOList.isNullOrEmpty()){
//            insertUserIds(visibleUserIdDTOList)
//        }
//
//        return id
//    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(draftNote: DraftNoteDTO): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertPollChoices(pollChoices: List<PollChoiceDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUserIds(userIds: List<UserIdDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertLocalFiles(files: List<DraftLocalFile>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDriveFiles(files: List<DriveFileRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDriveFile(file: DriveFileRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDraftLocalFile(file: DraftLocalFile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertFileRefs(files: List<DraftFileJunctionRef>): List<Long>

    fun getDraftNote(accountId: Long, draftNoteId: Long): DraftNote?{
        return getDraftNoteRelation(accountId, draftNoteId)?.toDraftNote(accountId)
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
    @Query("select * from draft_note_table where draft_note_id = :draftNoteId")
    abstract suspend fun findOne(draftNoteId: Long): DraftNoteRelation?

    @Transaction
    @Query("delete from 'draft_note_table' where accountId = :accountId and draft_note_id = :draftNoteId")
    abstract fun deleteDraftNote(accountId: Long, draftNoteId: Long)


    @Query("delete from 'draft_file_table' where draft_note_id = :draftNoteId and file_id = :fileId")
    abstract fun deleteFile(draftNoteId: Long, fileId: Long)

    @Transaction
    @Query("select * from draft_note_table where accountId = :accountId")
    abstract fun observeDraftNotesRelation(accountId: Long): Flow<List<DraftNoteRelation>>

    @Query("delete from draft_file_v2_table where draftNoteId = :draftNoteId")
    abstract fun deleteDraftJunctionFilesByDraftNoteId(draftNoteId: Long)
}