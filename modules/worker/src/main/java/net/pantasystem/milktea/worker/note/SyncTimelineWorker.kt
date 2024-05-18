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
import net.pantasystem.milktea.model.note.timeline.SyncTimelineUseCase
import java.util.concurrent.TimeUnit

@HiltWorker

class SyncTimelineWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val params: WorkerParameters,
    private val syncTimelineUseCase: SyncTimelineUseCase,
): CoroutineWorker(context, params) {

    companion object {
        const val WORKER_NAME = "SyncTimelineWorker"
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncTimelineWorker>(1, TimeUnit.MINUTES)
                .build()
        }

        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SyncTimelineWorker>()
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