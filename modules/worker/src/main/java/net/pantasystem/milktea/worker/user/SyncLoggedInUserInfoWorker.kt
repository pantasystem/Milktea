package net.pantasystem.milktea.worker.user

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
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncLoggedInUserInfoWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
) : CoroutineWorker(context, params) {

    companion object {
        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncLoggedInUserInfoWorker>(12, TimeUnit.HOURS)
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return accountRepository.findAll().mapCancellableCatching { accounts ->
            val userIds = accounts.map {
                User.Id(it.accountId, it.remoteId)
            }
            coroutineScope {
                userIds.map {
                    async {
                        userRepository.sync(it).getOrThrow()
                    }
                }
            }.awaitAll()
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