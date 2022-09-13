package jp.panta.misskeyandroidclient.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration

@InstallIn(SingletonComponent::class)
@EntryPoint
interface SubscriptionRegistrationWorkerProvider {
    fun subscriptionRegistration(): SubscriptionRegistration
}
class SubscriptionRegistrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TOKEN = "SubscriptionRegistrationWorker.TOKEN"
    }

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SubscriptionRegistrationWorkerProvider::class.java
        )
        return withContext(Dispatchers.IO) {
            entryPoint.subscriptionRegistration().registerAll()
            Result.success()
        }
    }
}