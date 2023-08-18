package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import net.pantasystem.milktea.model.note.draft.DraftNoteService
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.RememberVisibility
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val draftNoteRepository: DraftNoteRepository,
    private val settingRepository: LocalConfigRepository,
    private val draftNoteService: DraftNoteService,
) : UseCase {

    suspend operator fun invoke(createNote: CreateNote): Result<Note> {
        return runCancellableCatching {
            val createNoteResult = noteRepository.create(createNote)
            if (createNoteResult.isFailure) {
                draftNoteService.save(createNote)
            }
            val result = createNoteResult.getOrThrow()
            if (createNoteResult.isSuccess && createNote.draftNoteId != null) {
                draftNoteRepository.delete(createNote.draftNoteId)
            }
            setNoteVisibility(createNote)

            return@runCancellableCatching result
        }
    }

    suspend internal fun setNoteVisibility(createNote: CreateNote) {
        if (!(createNote.channelId == null && createNote.renoteId == null && createNote.replyId == null)) {
            return
        }
        val nowConfig =
            (settingRepository.getRememberVisibility(createNote.author.accountId).getOrThrow())

        when (nowConfig) {
            is RememberVisibility.None -> return
            is RememberVisibility.Remember -> settingRepository.save(
                nowConfig.copy(visibility = createNote.visibility)
            )
        }

    }


}