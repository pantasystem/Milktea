package jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft

import net.pantasystem.milktea.data.infrastructure.drive.DriveFileRecord
import net.pantasystem.milktea.data.infrastructure.drive.from
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.*
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import javax.inject.Inject

class DraftNoteRepositoryImpl @Inject constructor(
    val draftNoteDao: DraftNoteDao,
) : DraftNoteRepository {

    override suspend fun save(draftNote: DraftNote): Result<DraftNote> {
        return runCatching {
            val id = draftNoteDao.insert(DraftNoteDTO.make(draftNote))
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

            draftNoteDao.deleteDraftJunctionFilesByDraftNoteId(inserted.draftNoteId)

            val refs = draftNote.files?.map {
                when (it) {
                    is DraftNoteFile.Local -> {
                        DraftFileJunctionRef(
                            draftNoteId = inserted.draftNoteId,
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
                            draftNoteId = inserted.draftNoteId,
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
        return runCatching {

            val relation = draftNoteDao.findOne(draftNoteId) ?: throw NoSuchElementException()

            draftNoteDao.deleteDraftNote(
                relation.draftNoteDTO.accountId,
                relation.draftNoteDTO.draftNoteId
            )
        }
    }

}