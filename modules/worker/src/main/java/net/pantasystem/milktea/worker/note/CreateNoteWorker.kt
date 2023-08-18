package net.pantasystem.milktea.worker.note

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.ErrorType
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.MisskeyErrorCodes
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.note.CreateNoteUseCase
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import net.pantasystem.milktea.model.note.toCreateNote
import net.pantasystem.milktea.worker.WorkerTags
import java.io.IOException

@HiltWorker
class CreateNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val params: WorkerParameters,
    private val createNoteUseCase: CreateNoteUseCase,
    private val draftNoteRepository: DraftNoteRepository,
    private val accountRepository: AccountRepository,
    loggerFactory: Logger.Factory
) : CoroutineWorker(context, params) {

    enum class ErrorReasonType {
        NetworkError,
        FileUploadDeviceSecurityError,
        FileUploadDriveNoFreeSpaceError,
        ServerError,
        ClientError,
        UnauthorizedError,
        IAmAiError,
        ToManyRequestError,
        NotFoundError,
        UnknownError,
    }

    companion object {
        const val EXTRA_DRAFT_NOTE_ID = "DRAFT_NOTE_ID"
        const val EXTRA_NOTE_ID = "NOTE_ID"
        const val EXTRA_ACCOUNT_ID = "ACCOUNT_ID"

        const val EXTRA_FAILED_STACKTRACE = "FAILED_STACKTRACE"
        const val EXTRA_FAILED_MESSAGE = "FAILED_MESSAGE"
        const val EXTRA_FAILED_CAUSE = "FAILED_CAUSE"
        const val EXTRA_FAILED_THROWABLE_TO_STRING = "FAILED_THROWABLE_TO_STRING"
        const val EXTRA_FAILED_REASON = "FAILED_REASON"

        fun createWorker(draftNoteId: Long): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CreateNoteWorker>()
                .addTag(WorkerTags.CreateNote.name)
                .setInputData(
                    workDataOf(
                        EXTRA_DRAFT_NOTE_ID to draftNoteId,
                    )
                ).build()
        }
    }

    val logger = loggerFactory.create("CreateNoteWorker")

    override suspend fun doWork(): Result {
        val draftNoteId = params.inputData.getLong(EXTRA_DRAFT_NOTE_ID, -1)
        if (draftNoteId == -1L) {
            return Result.failure()
        }
        return draftNoteRepository.findOne(draftNoteId).mapCancellableCatching {
            it.toCreateNote(accountRepository.get(it.accountId).getOrThrow())
        }.mapCancellableCatching {
            createNoteUseCase.invoke(it).getOrThrow()
        }.onFailure {
            logger.error("Create Failed", it)
        }.fold(
            onSuccess = {
                Result.success(workDataOf(
                    EXTRA_NOTE_ID to it.id.noteId,
                    EXTRA_ACCOUNT_ID to it.id.accountId,
                ))
            },
            onFailure = {
                val data = workDataOf(
                    EXTRA_DRAFT_NOTE_ID to inputData.getLong(EXTRA_DRAFT_NOTE_ID, -1),
                    EXTRA_FAILED_STACKTRACE to it.stackTraceToString(),
                    EXTRA_FAILED_MESSAGE to it.message,
                    EXTRA_FAILED_CAUSE to it.cause?.toString(),
                    EXTRA_FAILED_THROWABLE_TO_STRING to it.toString(),
                    EXTRA_FAILED_REASON to convertToErrorReason(it).name,
                )
                Result.failure(data)
            }
        )
    }

    private fun convertToErrorReason(type: Throwable): ErrorReasonType {
        return when(type) {
            is APIError -> {
                when (type) {
                    is APIError.AuthenticationException -> ErrorReasonType.UnauthorizedError
                    is APIError.ClientException -> {
                        when(val errorType = type.error) {
                            is ErrorType.Misskey -> {
                                when(errorType.errorCodeeType) {
                                    MisskeyErrorCodes.NoFreeSpace -> ErrorReasonType.FileUploadDriveNoFreeSpaceError
                                    else -> ErrorReasonType.ClientError
                                }
                            }
                            is ErrorType.Raw -> ErrorReasonType.ClientError
                            null -> ErrorReasonType.ClientError
                        }
                    }
                    is APIError.ForbiddenException -> ErrorReasonType.UnauthorizedError
                    is APIError.IAmAIException -> ErrorReasonType.IAmAiError
                    is APIError.InternalServerException -> ErrorReasonType.ServerError
                    is APIError.NotFoundException -> ErrorReasonType.NotFoundError
                    is APIError.SomethingException -> ErrorReasonType.UnknownError
                    is APIError.ToManyRequestsException -> ErrorReasonType.ToManyRequestError
                }
            }
            is SecurityException -> {
                ErrorReasonType.FileUploadDeviceSecurityError
            }
            is IOException -> {
                ErrorReasonType.NetworkError
            }
            else -> ErrorReasonType.UnknownError
        }
    }
}