package net.pantasystem.milktea.worker.drive

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.note.NoteDataSource

@HiltWorker
class CleanupUnusedCacheWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val noteDataSource: NoteDataSource,
    private val loggerFactory: Logger.Factory
) : CoroutineWorker(context, params) {

    companion object {
        fun createOneTimeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CleanupUnusedCacheWorker>()
                .build()
        }
    }
    private val logger by lazy {
        loggerFactory.create("CleanupUnusedCacheWorker")
    }
    override suspend fun doWork(): Result {
        return try {
            if (noteDataSource.findLocalCount().getOrThrow() > 20000) {
                noteDataSource.clear().getOrThrow()
            }
            filePropertyDataSource.clearUnusedCaches().getOrThrow()

            Result.success()
        } catch (e: Exception) {
            logger.error("Failed to cleanup unused cache", e)
            Result.failure()
        }
    }
}