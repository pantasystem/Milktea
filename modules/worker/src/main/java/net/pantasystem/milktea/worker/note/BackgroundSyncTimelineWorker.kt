package net.pantasystem.milktea.worker.note

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.note.timeline.SyncTimelineFromLatestToCurrentUseCase
import java.util.concurrent.TimeUnit

@HiltWorker
class BackgroundSyncTimelineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val params: WorkerParameters,
    private val syncTimelineUseCase: SyncTimelineFromLatestToCurrentUseCase,
): CoroutineWorker(context, params) {

    companion object {
        const val WORKER_NAME = "BackgroundSyncTimelineWorker"
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<BackgroundSyncTimelineWorker>(30, TimeUnit.MINUTES)
                .build()
        }

        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<BackgroundSyncTimelineWorker>()
                .build()
        }
    }

    override suspend fun doWork(): Result {
        syncTimelineUseCase().fold(
            onSuccess = {
                return Result.success()
            },
            onFailure = {
                return Result.failure()
            }
        )
    }
}