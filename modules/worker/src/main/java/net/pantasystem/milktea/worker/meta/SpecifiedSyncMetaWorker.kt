package net.pantasystem.milktea.worker.meta

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import javax.inject.Inject

@HiltWorker
class SpecifiedSyncMetaWorker  @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val instanceInfoService: InstanceInfoService,
): CoroutineWorker(context, params) {

    companion object {
        const val EXTRA_INSTANCE_BASE_URL = "EXTRA_INSTANCE_BASE_URL"
        fun createOneTimeWorkRequest(instanceBaseUrl: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SpecifiedSyncMetaWorker>()
                .setInputData(
                    workDataOf(
                        EXTRA_INSTANCE_BASE_URL to instanceBaseUrl
                    )
                )
                .build()
        }
    }
    override suspend fun doWork(): Result {
        return instanceInfoService.sync(
            requireNotNull(params.inputData.getString(EXTRA_INSTANCE_BASE_URL))
        ).fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Result.failure()
            }
        )
    }
}

class SyncMetaExecutorImpl @Inject constructor(
    @ApplicationContext val context: Context
) : SyncMetaExecutor {
    override fun invoke(instanceBaseUrl: String) {
        WorkManager.getInstance(context).enqueue(
            SpecifiedSyncMetaWorker.createOneTimeWorkRequest(instanceBaseUrl)
        )

    }
}