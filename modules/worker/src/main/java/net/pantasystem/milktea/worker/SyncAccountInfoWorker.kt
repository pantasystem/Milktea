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
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncAccountInfoWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted val params: WorkerParameters,
    private val accountRepository: AccountRepository,
    private val nodeInfoRepository: NodeInfoRepository,
    private val userRepository: UserRepository,
    private val loggerFactory: Logger.Factory,
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
                try {
                    val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrThrow()
                    val user = userRepository.find(User.Id(account.accountId, account.remoteId))
                    val remoteSoftwareType = when (nodeInfo.type) {
                        is NodeInfo.SoftwareType.Firefish -> Account.InstanceType.FIREFISH
                        is NodeInfo.SoftwareType.Mastodon -> Account.InstanceType.MASTODON
                        is NodeInfo.SoftwareType.Misskey -> Account.InstanceType.MISSKEY
                        is NodeInfo.SoftwareType.Pleroma -> Account.InstanceType.PLEROMA
                        is NodeInfo.SoftwareType.Other -> throw IllegalStateException("unknown type of software:${nodeInfo.type}")
                    }
                    if (account.instanceType != remoteSoftwareType || user.userName != account.userName) {
                        accountRepository.add(
                            account.copy(
                                instanceType = remoteSoftwareType,
                                userName = account.userName
                            ), false
                        )
                    }
                    true
                } catch (e: Exception) {
                    logger.error("failed to sync account info", e)
                    false
                }
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