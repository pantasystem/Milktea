package jp.panta.misskeyandroidclient.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SubscriptionRegistrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TOKEN = "SubscriptionRegistrationWorker.TOKEN"
    }

    override suspend fun doWork(): Result {
        val token = inputData.getString(TOKEN)
            ?: return Result.failure()
        return withContext(Dispatchers.IO) {
            val miCore = applicationContext as MiCore
            miCore.getSubscriptionRegistration().registerAll(token)
            Result.success()
        }
    }
}