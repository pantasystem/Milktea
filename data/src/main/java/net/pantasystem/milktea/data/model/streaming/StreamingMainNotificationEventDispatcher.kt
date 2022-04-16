package net.pantasystem.milktea.data.model.streaming

import jp.panta.misskeyandroidclient.gettters.NotificationRelationGetter
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.streaming.ChannelBody

class StreamingMainNotificationEventDispatcher(
    private val notificationRelationGetter: NotificationRelationGetter,
    private val unreadNotificationDAO: UnreadNotificationDAO
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.Notification)?.let {
            notificationRelationGetter.get(account, it.body)
        } != null || (mainEvent as? ChannelBody.Main.ReadAllNotifications)?.let {
            unreadNotificationDAO.deleteWhereAccountId(account.accountId)
        } != null
    }
}