package net.pantasystem.milktea.worker.filter

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
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.filter.MastodonWordFilterRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncMastodonFilterWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val wordFilterRepository: MastodonWordFilterRepository,
) : CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkerRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncMastodonFilterWorker>(2, TimeUnit.DAYS)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return accountRepository.findAll().mapCancellableCatching { accounts ->
            accounts.filter {
                it.instanceType == Account.InstanceType.MASTODON || it.instanceType == Account.InstanceType.PLEROMA
            }
        }.mapCancellableCatching { accounts ->
            coroutineScope {
                accounts.map {
                    async {
                        wordFilterRepository.sync(it.accountId)
                    }
                }.awaitAll()
            }
        }.mapCancellableCatching { results ->
            results.map {
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