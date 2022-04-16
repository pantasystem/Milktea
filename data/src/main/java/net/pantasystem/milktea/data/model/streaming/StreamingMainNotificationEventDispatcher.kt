package net.pantasystem.milktea.data.model.streaming

import net.pantasystem.milktea.data.gettters.NotificationRelationGetter
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.streaming.ChannelBody


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