package net.pantasystem.milktea.worker.user.renote.mute

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository

class SyncRenoteMutesWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val renoteMuteRepository: RenoteMuteRepository,
) : CoroutineWorker(context, params) {

    companion object {
        fun createOneTimeWorkRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SyncRenoteMutesWorker>()
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return accountRepository.findAll().mapCancellableCatching { accounts ->
            coroutineScope {
                accounts.map {
                    async {
                        renoteMuteRepository.syncBy(it.accountId)
                    }
                }
            }.awaitAll()
        }.mapCancellableCatching { list ->
            list.map {
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