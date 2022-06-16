package net.pantasystem.milktea.data.infrastructure.notes.impl

import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.draft.*
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.model.notes.type
import net.pantasystem.milktea.model.notes.visibleUserIds
import javax.inject.Inject

class DraftNoteServiceImpl @Inject constructor(
    private val draftNoteRepository: DraftNoteRepository,
    private val driveFileRepository: DriveFileRepository,
) : DraftNoteService {

    override suspend fun save(createNote: CreateNote): Result<DraftNote> {
        return runCatching {
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
                    reservationPostingAt = null,
                    channelId = createNote.channelId,
                    draftNoteId = createNote.draftNoteId ?: 0L
                )
            ).getOrThrow()
        }

    }
}