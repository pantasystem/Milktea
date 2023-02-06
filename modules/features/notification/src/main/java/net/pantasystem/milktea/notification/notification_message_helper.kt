package net.pantasystem.milktea.notification

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import net.pantasystem.milktea.model.notification.*


fun Context.notificationMessageScope(block: NotificationMessageScope.() -> Unit) {
    block.invoke(NotificationMessageScope(this))
}

class NotificationMessageScope(val context: Context) {
    fun NotificationRelation.showSnackBarMessage(view: View) {
        val message = getMessage()
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun NotificationRelation.getMessage(): String {
        return when (val notification = this.notification) {
            is FollowNotification -> context.getString(
                R.string.followed_by,
                this.user?.displayUserName ?: ""
            )
            is MentionNotification -> context.getString(
                R.string.mention_by,
                this.user?.displayUserName ?: ""
            )
            is ReplyNotification -> context.getString(
                R.string.replied_by,
                this.user?.displayUserName ?: ""
            )
            is RenoteNotification -> context.getString(
                R.string.renoted_by,
                this.user?.displayUserName ?: ""
            )
            is QuoteNotification -> context.getString(
                R.string.quoted_by,
                this.user?.displayUserName ?: ""
            )
            is ReactionNotification -> context.getString(
                R.string.reacted_by,
                this.user?.displayUserName ?: ""
            )
            is PollVoteNotification -> context.getString(
                R.string.voted_by,
                this.user?.displayUserName ?: ""
            )
            is ReceiveFollowRequestNotification -> context.getString(
                R.string.follow_requested_by,
                this.user?.displayUserName ?: ""
            )
            is FollowRequestAcceptedNotification -> context.getString(
                R.string.follow_request_accepted_by,
                this.user?.displayUserName ?: ""
            )
            is PollEndedNotification -> context.getString(R.string.poll_ended)
            is GroupInvitedNotification -> context.getString(
                R.string.notification_group_invited_message,
                notification.group.name
            )
            is UnknownNotification -> context.getString(R.string.unknown_notification)
            is FavoriteNotification -> context.getString(
                R.string.notification_favorited_by,
                user?.displayUserName ?: ""
            )
            is StatusNotification -> context.getString(
                R.string.notification_posted_by,
                user?.displayUserName ?: ""
            )
        }
    }
}