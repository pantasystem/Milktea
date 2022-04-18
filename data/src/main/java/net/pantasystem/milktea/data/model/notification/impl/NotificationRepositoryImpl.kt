package net.pantasystem.milktea.data.model.notification.impl

import net.pantasystem.milktea.data.gettters.NotificationRelationGetter
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationRepository
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.streaming.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDataSource: NotificationDataSource,
    private val socketProvider: SocketWithAccountProvider,
    private val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
    private val notificationRelationGetter: NotificationRelationGetter,
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationRepository {




    override suspend fun read(notificationId: Notification.Id) {
        runCatching {
            val account = accountRepository.get(notificationId.accountId)
            socketProvider.get(account).send(Send.ReadNotification(Send.ReadNotification.Body(notificationId.notificationId)).toJson())
            notificationDataSource.add(notificationDataSource.get(notificationId).read())
        }
    }

    override fun countUnreadNotification(accountId: Long): Flow<Int> {
        return unreadNotificationDAO.countByAccountId(accountId)
    }



    suspend fun dispatch(accountId: Long, notification: ChannelBody.Main.Notification) {
        val account = accountRepository.get(accountId)
        notificationRelationGetter.get(account, notification.body)
    }


}