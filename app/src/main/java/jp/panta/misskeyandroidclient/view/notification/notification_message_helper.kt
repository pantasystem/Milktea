package jp.panta.misskeyandroidclient.view.notification

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notification.*


fun Context.notificationMessageScope(block: NotificationMessageScope.()->Unit) {
    block.invoke(NotificationMessageScope(this))
}

class NotificationMessageScope(val context: Context) {
    fun NotificationRelation.showSnackBarMessage(view: View) {
        val message = getMessage()
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    fun NotificationRelation.getMessage() : String{
        return when(this.notification) {
            is FollowNotification -> context.getString(R.string.followed_by, this.user.getDisplayUserName())
            is MentionNotification -> context.getString(R.string.mention_by, this.user.getDisplayUserName())
            is ReplyNotification -> context.getString(R.string.replied_by, this.user.getDisplayUserName())
            is RenoteNotification -> context.getString(R.string.renoted_by, this.user.getDisplayUserName())
            is QuoteNotification -> context.getString(R.string.quoted_by, this.user.getDisplayUserName())
            is ReactionNotification -> context.getString(R.string.reacted_by, this.user.getDisplayUserName())
            is PollVoteNotification -> context.getString(R.string.voted_by, this.user.getDisplayUserName())
            is ReceiveFollowRequestNotification -> context.getString(R.string.follow_requested_by, this.user.getDisplayUserName())
            is FollowRequestAcceptedNotification -> context.getString(R.string.follow_request_accepted_by, this.user.getDisplayUserName())
            is UnknownNotification -> context.getString(R.string.unknown_notification)
        }
    }
}