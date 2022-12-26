package net.pantasystem.milktea.data.infrastructure.notes.draft

import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.data.infrastructure.drive.from
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.*
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import javax.inject.Inject

class DraftNoteRepositoryImpl @Inject constructor(
    val draftNoteDao: DraftNoteDao,
    val loggerFactory: Logger.Factory
) : DraftNoteRepository {
    val logger by lazy {
        loggerFactory.create("DraftNoteRepositoryImpl")
    }

    override suspend fun save(draftNote: DraftNote): Result<DraftNote> {
        return runCancellableCatching {
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
                                )
                            ),
                            filePropertyId = null,
                        )
                    }
                    is DraftNoteFile.Remote -> {
                        DraftFileJunctionRef(
                            draftNoteId = id,
                            filePropertyId = draftNoteDao.insertDriveFile(DriveFileRecord.from(it.fileProperty)),
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

    override suspend fun delete(draftNoteId: Long): Result<Unit> {
        return runCancellableCatching {

            val relation = draftNoteDao.findOne(draftNoteId) ?: throw NoSuchElementException()

            draftNoteDao.deleteDraftNote(
                relation.draftNoteDTO.accountId,
                relation.draftNoteDTO.draftNoteId!!
            )
        }
    }

    override suspend fun findOne(draftNoteId: Long): Result<DraftNote> {
        return runCancellableCatching {
            val relation = draftNoteDao.findOne(draftNoteId) ?: throw NoSuchElementException()
            relation.toDraftNote(relation.draftNoteDTO.accountId)
        }
    }


}