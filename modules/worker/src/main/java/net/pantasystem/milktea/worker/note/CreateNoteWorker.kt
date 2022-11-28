package net.pantasystem.milktea.worker.note

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.CreateNoteUseCase
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.toCreateNote
import net.pantasystem.milktea.model.notes.toNoteEditingState
import net.pantasystem.milktea.worker.WorkerTags

@HiltWorker
class CreateNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val params: WorkerParameters,
    private val createNoteUseCase: CreateNoteUseCase,
    private val draftNoteRepository: DraftNoteRepository,
    private val accountRepository: AccountRepository,
) : CoroutineWorker(context, params) {
    companion object {
        const val EXTRA_DRAFT_NOTE_ID = "DRAFT_NOTE_ID"
        const val EXTRA_NOTE_ID = "NOTE_ID"
        const val EXTRA_ACCOUNT_ID = "ACCOUNT_ID"

        fun createWorker(draftNoteId: Long): WorkRequest {
            return OneTimeWorkRequestBuilder<CreateNoteWorker>()
                .addTag(WorkerTags.CreateNote.name)
                .setInputData(
                    workDataOf(
                        EXTRA_DRAFT_NOTE_ID to draftNoteId,
                    )
                ).build()
        }
    }

    override suspend fun doWork(): Result {
        val draftNoteId = params.inputData.getLong(EXTRA_DRAFT_NOTE_ID, -1)
        if (draftNoteId == -1L) {
            return Result.failure()
        }

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.getWorkInfosByTagLiveData(WorkerTags.CreateNote.name).observeForever {
            it.map {

            }
        }
        return draftNoteRepository.findOne(draftNoteId).mapCatching {
            it.toNoteEditingState().toCreateNote(accountRepository.get(it.accountId).getOrThrow())
        }.mapCatching {
            createNoteUseCase.invoke(it).getOrThrow()
        }.fold(
            onSuccess = {
                Result.success(workDataOf(
                    EXTRA_NOTE_ID to it.id.noteId,
                    EXTRA_ACCOUNT_ID to it.id.accountId,
                ))
            },
            onFailure = {
                Result.failure(params.inputData)
            }
        )
    }
}