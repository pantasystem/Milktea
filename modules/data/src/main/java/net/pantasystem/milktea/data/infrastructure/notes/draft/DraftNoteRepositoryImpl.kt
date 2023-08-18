package net.pantasystem.milktea.data.infrastructure.notes.draft

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.data.infrastructure.drive.from
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.*
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteFile
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import javax.inject.Inject

class DraftNoteRepositoryImpl @Inject constructor(
    val draftNoteDao: DraftNoteDao,
    val loggerFactory: Logger.Factory,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : DraftNoteRepository {
    val logger by lazy {
        loggerFactory.create("DraftNoteRepositoryImpl")
    }

    override suspend fun save(draftNote: DraftNote): Result<DraftNote> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val id = draftNoteDao.insert(DraftNoteDTO.make(draftNote))

                logger.debug("draftNoteId:$id")
                val inserted = draftNote.copy(draftNoteId = id)
                val pollChoices = draftNote.draftPoll?.choices?.let {
                    it.mapIndexed { index, s ->
                        PollChoiceDTO(
                            choice = s,
                            draftNoteId = id,
                            weight = index
                        )
                    }
                }
                val visibleUserIdDTOList = draftNote.visibleUserIds?.map {
                    UserIdDTO(draftNoteId = id, userId = it)
                }

                draftNoteDao.deleteDraftJunctionFilesByDraftNoteId(id)

                val refs = draftNote.draftFiles?.map {
                    when (it) {
                        is DraftNoteFile.Local -> {
                            DraftFileJunctionRef(
                                draftNoteId = id,
                                localFileId = draftNoteDao.insertDraftLocalFile(
                                    DraftLocalFile(
                                        localFileId = it.localFileId,
                                        filePath = it.filePath,
                                        folderId = it.folderId,
                                        name = it.name,
                                        type = it.type,
                                        thumbnailUrl = it.thumbnailUrl,
                                        isSensitive = it.isSensitive,
                                        fileSize = it.fileSize,
                                        comment = it.comment
                                    )
                                ),
                                filePropertyId = null,
                            )
                        }
                        is DraftNoteFile.Remote -> {
                            DraftFileJunctionRef(
                                draftNoteId = id,
                                filePropertyId = draftNoteDao.insertDriveFile(
                                    DriveFileRecord.from(
                                        it.fileProperty
                                    )
                                ),
                                localFileId = null,
                            )
                        }
                    }
                } ?: emptyList()
                draftNoteDao.insertFileRefs(refs)

                if (!pollChoices.isNullOrEmpty()) {
                    draftNoteDao.insertPollChoices(pollChoices)
                }
                if (!visibleUserIdDTOList.isNullOrEmpty()) {
                    draftNoteDao.insertUserIds(visibleUserIdDTOList)
                }

                draftNoteDao.getDraftNote(inserted.accountId, inserted.draftNoteId)!!

            }
        }
    }

    override suspend fun delete(draftNoteId: Long): Result<Unit> {
        return runCancellableCatching {
            withContext(ioDispatcher) {

                val relation = draftNoteDao.findOne(draftNoteId) ?: throw NoSuchElementException()

                draftNoteDao.deleteDraftNote(
                    relation.draftNoteDTO.accountId,
                    relation.draftNoteDTO.draftNoteId!!
                )
            }
        }
    }

    override suspend fun findOne(draftNoteId: Long): Result<DraftNote> {
        return runCancellableCatching {
            withContext(ioDispatcher) {
                val relation = draftNoteDao.findOne(draftNoteId) ?: throw NoSuchElementException()
                relation.toDraftNote(relation.draftNoteDTO.accountId)
            }
        }
    }

    override fun observeByAccountId(accountId: Long): Flow<List<DraftNote>> {
        return draftNoteDao.observeDraftNotesRelation(accountId).map { notes ->
            notes.map {
                it.toDraftNote(accountId)
            }
        }.flowOn(ioDispatcher)
    }


}