package jp.panta.misskeyandroidclient.model.streaming

import jp.panta.misskeyandroidclient.gettters.NotificationRelationGetter
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.streaming.ChannelBody

class StreamingMainNotificationEventDispatcher(
    private val notificationRelationGetter: NotificationRelationGetter,
    private val notificationDataSource: NotificationDataSource
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.Notification)?.let {
            notificationRelationGetter.get(account, it.body)
        } != null || (mainEvent as? ChannelBody.Main.ReadAllNotifications)?.let {
            notificationDataSource.readAllNotification(account.accountId)
        } != null
    }
}