package net.pantasystem.milktea.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncNodeInfoCacheWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val nodeInfoRepository: NodeInfoRepository,
) : CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncNodeInfoCacheWorker>(12, TimeUnit.HOURS)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return nodeInfoRepository.syncAll().fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Result.failure()
            }
        )
    }
}