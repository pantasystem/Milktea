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
import net.pantasystem.milktea.common.Logger
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
    private val loggerFactory: Logger.Factory,
): CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncMetaWorker>(12, TimeUnit.HOURS)
                .build()
        }
    }

    private val logger by lazy {
        loggerFactory.create("SyncMetaWorker")
    }

    override suspend fun doWork(): Result {
        return accountRepository.findAll().mapCancellableCatching { accounts ->
            val domains = accounts.map { it.normalizedInstanceUri }.distinct()
            coroutineScope {
                domains.map {
                    async {
                        instanceInfoService.sync(it)
                    }
                }.awaitAll()
            }.count { result ->
                result.onFailure {
                    logger.error("Fetch instance info failed", it)
                }.isSuccess
            } == domains.size
        }.fold(
            onSuccess = {
                if (it) Result.success() else Result.failure()
            },
            onFailure = {
                Result.failure()
            }
        )
    }
}