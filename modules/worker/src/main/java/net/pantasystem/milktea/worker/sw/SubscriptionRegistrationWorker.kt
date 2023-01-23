package net.pantasystem.milktea.worker.sw

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration


@HiltWorker
class SubscriptionRegistrationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val subscriptionRegistration: SubscriptionRegistration,
) : CoroutineWorker(context, params) {

    companion object {
        const val TOKEN = "SubscriptionRegistrationWorker.TOKEN"
    }

    override suspend fun doWork(): Result {
        return try {
            return withContext(Dispatchers.IO) {
                subscriptionRegistration.registerAll()
                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}