package net.pantasystem.milktea.data.infrastructure.notes.impl

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftLocalFile
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.from
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.DraftNoteSavedEvent
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.model.notes.draft.DraftPoll
import net.pantasystem.milktea.model.notes.draft.from
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.model.notes.type
import net.pantasystem.milktea.model.notes.visibleUserIds
import java.util.Date
import javax.inject.Inject

class DraftNoteServiceImpl @Inject constructor(
    private val draftNoteRepository: DraftNoteRepository,
    private val driveFileRepository: DriveFileRepository,
    private val draftNoteDao: DraftNoteDao,
) : DraftNoteService {

    private val draftNoteSavedEvent = MutableSharedFlow<DraftNoteSavedEvent>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 256
    )

    override fun getDraftNoteSavedEventBy(accountId: Long): Flow<DraftNoteSavedEvent> {
        return draftNoteSavedEvent
    }

    override suspend fun save(createNote: CreateNote): Result<DraftNote> {
        return runCancellableCatching {
            val draftFiles = createNote.files?.map {
                when (it) {
                    is AppFile.Remote -> {
                        DraftNoteFile.Remote(
                            driveFileRepository.find(it.id)
                        )
                    }
                    is AppFile.Local -> {
                        DraftNoteFile.Local.from(it)
                    }
                }
            } ?: emptyList()
            val draftPoll = createNote.poll?.let {
                DraftPoll(it.choices, it.multiple, it.expiresAt)
            }

            draftNoteRepository.save(
                DraftNote(
                    accountId = createNote.author.accountId,
                    text = createNote.text,
                    visibility = createNote.visibility.type(),
                    visibleUserIds = createNote.visibleUserIds(),
                    cw = createNote.cw,
                    draftFiles = draftFiles,
                    viaMobile = createNote.viaMobile,
                    localOnly = createNote.visibility.isLocalOnly(),
                    noExtractEmojis = createNote.noExtractEmojis,
                    noExtractHashtags = createNote.noExtractHashtags,
                    noExtractMentions = createNote.noExtractMentions,
                    replyId = createNote.replyId?.noteId,
                    renoteId = createNote.renoteId?.noteId,
                    draftPoll = draftPoll,
                    reservationPostingAt = createNote.scheduleWillPostAt?.toEpochMilliseconds()
                        ?.let {
                            Date(it)
                        },
                    channelId = createNote.channelId,
                    draftNoteId = createNote.draftNoteId ?: 0L,
                    isSensitive = createNote.isSensitive,
                    reactionAcceptanceType = createNote.reactionAcceptance
                )
            ).getOrThrow()
        }.onFailure {
            draftNoteSavedEvent.tryEmit(DraftNoteSavedEvent.Failed(createNote, it))
        }.onSuccess {
            draftNoteSavedEvent.tryEmit(DraftNoteSavedEvent.Success(it))
        }

    }

    override suspend fun save(draftNoteFile: DraftNoteFile): Result<DraftNoteFile> {
        return runCancellableCatching {
            when (draftNoteFile) {
                is DraftNoteFile.Local -> {
                    draftNoteDao.insertDraftLocalFile(DraftLocalFile.from(draftNoteFile))
                    draftNoteFile
                }
                is DraftNoteFile.Remote -> {
                    driveFileRepository.update(
                        draftNoteFile.fileProperty.update()
                    )
                    DraftNoteFile.Remote(
                        driveFileRepository.find(draftNoteFile.fileProperty.id)
                    )
                }
            }

        }
    }
}