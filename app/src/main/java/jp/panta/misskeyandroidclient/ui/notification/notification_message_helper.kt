package jp.panta.misskeyandroidclient.ui.notification

import android.content.Context
import android.view.View
import net.pantasystem.milktea.data.model.notification.*


fun Context.notificationMessageScope(block: NotificationMessageScope.() -> Unit) {
    block.invoke(NotificationMessageScope(this))
}

class NotificationMessageScope(val context: Context) {
    fun net.pantasystem.milktea.model.notification.NotificationRelation.showSnackBarMessage(view: View) {
        val message = getMessage()
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun net.pantasystem.milktea.model.notification.NotificationRelation.getMessage(): String {
        return when (this.notification) {
            is net.pantasystem.milktea.model.notification.FollowNotification -> context.getString(
                R.string.followed_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.MentionNotification -> context.getString(
                R.string.mention_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.ReplyNotification -> context.getString(
                R.string.replied_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.RenoteNotification -> context.getString(
                R.string.renoted_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.QuoteNotification -> context.getString(
                R.string.quoted_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.ReactionNotification -> context.getString(
                R.string.reacted_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.PollVoteNotification -> context.getString(
                R.string.voted_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.ReceiveFollowRequestNotification -> context.getString(
                R.string.follow_requested_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.FollowRequestAcceptedNotification -> context.getString(
                R.string.follow_request_accepted_by,
                this.user?.getDisplayUserName() ?: ""
            )
            is net.pantasystem.milktea.model.notification.PollEndedNotification -> context.getString(R.string.poll_ended)
            is net.pantasystem.milktea.model.notification.UnknownNotification -> context.getString(R.string.unknown_notification)
        }
    }
}