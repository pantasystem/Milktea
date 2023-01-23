package net.pantasystem.milktea.data.infrastructure.notification.impl

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.api_streaming.Send
import net.pantasystem.milktea.api_streaming.toJson
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDataSource: NotificationDataSource,
    private val socketProvider: SocketWithAccountProvider,
    private val accountRepository: AccountRepository,
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationRepository {




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