package net.pantasystem.milktea.worker.meta

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
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncMetaWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val instanceInfoService: InstanceInfoService,
    private val accountRepository: AccountRepository,
): CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncMetaWorker>(12, TimeUnit.HOURS)
                .build()
        }
    }
    override suspend fun doWork(): Result {
        return accountRepository.findAll().mapCancellableCatching { accounts ->
            coroutineScope {
                accounts.map {
                    async {
                        instanceInfoService.sync(it.normalizedInstanceUri)
                    }
                }.awaitAll()
            }.forEach {
                it.getOrThrow()
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