package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api_streaming.Send
import net.pantasystem.milktea.api_streaming.toJson
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.markers.MarkerRepository
import net.pantasystem.milktea.model.markers.SaveMarkerParams
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDataSource: NotificationDataSource,
    private val socketProvider: SocketWithAccountProvider,
    private val accountRepository: AccountRepository,
    private val unreadNotificationDAO: UnreadNotificationDAO,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val markerRepository: MarkerRepository,
) : NotificationRepository {


    override suspend fun markAsRead(accountId: Long): Result<Unit> = runCancellableCatching {
        val account = accountRepository.get(accountId).getOrThrow()
        when(account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                misskeyAPIProvider.get(account).markAllAsReadNotifications(
                    I(
                        account.token
                    )
                ).throwIfHasError()
            }
            Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                val latest = unreadNotificationDAO.getLatestUnreadId(accountId)
                    ?: return@runCancellableCatching
                markerRepository.save(
                    accountId,
                    SaveMarkerParams(
                        notifications = latest.notificationId
                    )
                ).getOrThrow()
            }
        }
    }

    override suspend fun read(notificationId: Notification.Id) {
        runCancellableCatching {
            val account = accountRepository.get(notificationId.accountId).getOrThrow()
            socketProvider.get(account)?.send(
                Send.ReadNotification(
                    Send.ReadNotification.Body(notificationId.notificationId)).toJson())
            notificationDataSource.add(notificationDataSource.get(notificationId).getOrThrow().read())
        }
    }

    override fun countUnreadNotification(accountId: Long): Flow<Int> {
        return unreadNotificationDAO.countByAccountId(accountId)
    }


}