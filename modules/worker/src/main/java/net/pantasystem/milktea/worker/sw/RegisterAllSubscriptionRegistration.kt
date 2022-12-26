package net.pantasystem.milktea.worker.sw

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration

@HiltWorker
class RegisterAllSubscriptionRegistration @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val subscriptionRegistration: SubscriptionRegistration,
) : CoroutineWorker(context, params) {

    companion object {
        fun createWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RegisterAllSubscriptionRegistration>()
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return runCancellableCatching {
            subscriptionRegistration.registerAll()
        }.fold(
            onSuccess = {
                Result.success()
            },
            onFailure = {
                Result.failure()
            }
        )

    }
}