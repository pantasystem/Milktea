package jp.panta.misskeyandroidclient.model.notification.impl

import jp.panta.misskeyandroidclient.gettters.NotificationRelationGetter
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotification
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.streaming.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationDataSource: NotificationDataSource,
    private val socketProvider: SocketWithAccountProvider,
    private val accountRepository: AccountRepository,
    private val notificationRelationGetter: NotificationRelationGetter,
    private val unreadNotificationDAO: UnreadNotificationDAO
) : NotificationRepository{




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