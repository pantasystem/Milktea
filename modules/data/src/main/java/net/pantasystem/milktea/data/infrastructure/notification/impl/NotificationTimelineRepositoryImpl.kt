package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notification.db.NotificationCacheDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationTimelineRepository
import javax.inject.Inject

class NotificationTimelineRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val accountRepository: AccountRepository,
    private val notificationAdder: NotificationCacheAdder,
    private val coroutineScope: CoroutineScope,
    private val notificationCacheDAO: NotificationCacheDAO,
) : NotificationTimelineRepository {

    // 最後に初期読み込みした時間をマップに保存しておく(key = accountId, value = lastFetchTime)
    private var lastFetchTimeMap = mapOf<Long, Long>()
    override suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String?,
        limit: Int
    ): Result<List<Notification>> = runCancellableCatching {
        val account = accountRepository.get(accountId).getOrThrow()
        val models = if (untilId == null) {
            notificationCacheDAO.findNotifications(accountId, limit)
        } else {
            notificationCacheDAO.findNotificationsByUntilId(accountId, untilId, limit)
        }.map {
            it.toModel()
        }
        val lastInitialFetchTime = lastFetchTimeMap[accountId] ?: System.currentTimeMillis()
        val now = System.currentTimeMillis()
        val fetched = if (models.size < limit) {
            fetch(account, untilId).getOrThrow()
        } else {
            coroutineScope.launch {
                if (now - lastInitialFetchTime > 1000 * 60 * 3) {
                    fetch(account, untilId)
                }
            }
            models
        }

        if (untilId == null) {
            lastFetchTimeMap += accountId to now
        }

        fetched
    }

    private suspend fun fetch(account: Account, untilId: String?): Result<List<Notification>> = runCancellableCatching {
        when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                fetchMisskeyNotifications(account, untilId).map {
                    notificationAdder.addAndConvert(account, it).notification
                }
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                fetchMastodonNotifications(account, untilId).map {
                    notificationAdder.addConvert(account, it).notification
                }
            }
        }
    }
    private suspend fun fetchMisskeyNotifications(account: Account, untilId: String?): List<NotificationDTO> {
        val res = misskeyAPIProvider.get(account).notification(
            NotificationRequest(
                i = account.token,
                untilId = untilId,
            )
        ).throwIfHasError()
        return res.body() ?: emptyList()
    }

    private suspend fun fetchMastodonNotifications(account: Account, untilId: String?): List<MstNotificationDTO> {
        val res = mastodonAPIProvider.get(account).getNotifications(
            maxId = untilId
        )
        return res.body() ?: emptyList()
    }
}