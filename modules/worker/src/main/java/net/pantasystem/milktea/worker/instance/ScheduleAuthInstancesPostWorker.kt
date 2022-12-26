package net.pantasystem.milktea.worker.instance

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.InstanceInfoRepository
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class ScheduleAuthInstancesPostWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val instanceInfoRepository: InstanceInfoRepository,
    private val configRepository: LocalConfigRepository
) : CoroutineWorker(context, params){

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<ScheduleAuthInstancesPostWorker>(7, TimeUnit.DAYS)
                .build()
        }
    }
    override suspend fun doWork(): Result {
        return coroutineScope {
            runCancellableCatching {
                val config = configRepository.get().getOrThrow()
                if (config.isAnalyticsCollectionEnabled.isEnabled) {

                    accountRepository.findAll().map { accounts ->
                        accounts.map {
                            it.getHost()
                        }.distinct()
                    }.map { hosts ->
                        hosts.map {
                            async {
                                instanceInfoRepository.postInstance(it)
                            }
                        }.awaitAll()
                    }.getOrThrow()
                }
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
}