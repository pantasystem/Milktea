package net.pantasystem.milktea.data.infrastructure.streaming

import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.data.infrastructure.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationCacheAdder
import net.pantasystem.milktea.model.account.Account


class StreamingMainNotificationEventDispatcher(
    private val unreadNotificationDAO: UnreadNotificationDAO,
    private val notificationCacheAdder: NotificationCacheAdder,
) : StreamingMainEventDispatcher{

    override suspend fun dispatch(account: Account, mainEvent: ChannelBody.Main): Boolean {
        return (mainEvent as? ChannelBody.Main.Notification)?.let {
            notificationCacheAdder.addAndConvert(account, it.body)
        } != null || (mainEvent as? ChannelBody.Main.ReadAllNotifications)?.let {
            unreadNotificationDAO.deleteWhereAccountId(account.accountId)
        } != null
    }
}