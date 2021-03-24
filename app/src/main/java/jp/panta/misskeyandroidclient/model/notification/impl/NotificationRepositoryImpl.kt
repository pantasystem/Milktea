package jp.panta.misskeyandroidclient.model.notification.impl

import jp.panta.misskeyandroidclient.gettters.NotificationRelationGetter
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.streaming.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationRepositoryImpl(
    val notificationDataSource: NotificationDataSource,
    val coroutineScope: CoroutineScope,
    val socketProvider: SocketWithAccountProvider,
    val accountRepository: AccountRepository,
    val notificationRelationGetter: NotificationRelationGetter,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : NotificationRepository{

    private val unreadNotificationCountStateMap = mutableMapOf<Long, MutableStateFlow<Int>>()
    private val lock = Mutex()

    init {
        notificationDataSource.addEventListener {
            coroutineScope.launch(dispatcher) {
                lock.withLock {
                    unreadNotificationCountStateMap.clear()

                    getFlow(it.notificationId.accountId).value = notificationDataSource.countUnreadNotification(it.notificationId.accountId)
                }
            }
        }
    }

    override suspend fun read(notificationId: Notification.Id) {
        runCatching {
            val account = accountRepository.get(notificationId.accountId)
            socketProvider.get(account).send(Send.ReadNotification(Send.ReadNotification.Body(notificationId.notificationId)).toJson())
            notificationDataSource.add(notificationDataSource.get(notificationId).read())
        }
    }

    override fun countUnreadNotification(accountId: Long): Flow<Int> {
        return getFlow(accountId)
    }

    private fun getFlow(accountId: Long): MutableStateFlow<Int> {
        synchronized(unreadNotificationCountStateMap) {
            var flow = unreadNotificationCountStateMap[accountId]
            if(flow == null) {
                flow = MutableStateFlow(0)
                unreadNotificationCountStateMap[accountId] = flow
            }
            return flow
        }
    }

    suspend fun dispatch(accountId: Long, notification: ChannelBody.Main.Notification) {
        val account = accountRepository.get(accountId)
        notificationRelationGetter.get(account, notification.body)
    }


}