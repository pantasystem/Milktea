package net.pantasystem.milktea.worker.drive

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.drive.FilePropertyDataSource

@HiltWorker
class CleanupUnusedDriveCacheWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val filePropertyDataSource: FilePropertyDataSource,
) : CoroutineWorker(context, params) {
    companion object {
        fun createOneTimeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CleanupUnusedDriveCacheWorker>()
                .build()
        }
    }
    override suspend fun doWork(): Result {
        return filePropertyDataSource.clearUnusedCaches().fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Result.failure()
            }
        )
    }
}