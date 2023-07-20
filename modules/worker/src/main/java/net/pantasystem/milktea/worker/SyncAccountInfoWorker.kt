package net.pantasystem.milktea.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.SyncAccountInfoUseCase
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncAccountInfoWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val loggerFactory: Logger.Factory,
    private val syncAccountInfoUseCase: SyncAccountInfoUseCase,
) : CoroutineWorker(context, params) {

    private val logger by lazy {
        loggerFactory.create("SyncAccountInfoWorker")
    }

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncAccountInfoWorker>(90, TimeUnit.DAYS)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        try {
            val accounts = accountRepository.findAll().getOrThrow()
            if (accounts.isEmpty()) {
                return Result.success()
            }

            val successfulCounts = accounts.map { account ->
                syncAccountInfoUseCase(account).onFailure {
                    logger.error("failed to sync account info", it)
                }.fold(
                    onSuccess = {
                        true
                    },
                    onFailure = {
                        false
                    }
                )
            }.count {
                it
            }
            if (successfulCounts <= 0) {
                return Result.failure()
            }

        } catch (e: Exception) {
            return Result.failure()
        }

        return Result.success()
    }
}