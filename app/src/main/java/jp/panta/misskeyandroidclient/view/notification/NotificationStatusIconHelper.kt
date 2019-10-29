package jp.panta.misskeyandroidclient.view.notification

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.reaction.ReactionViewHelper

object NotificationStatusIconHelper {

    @JvmStatic
    @BindingAdapter("notificationStatusView", "notificationReactionImageView", "notificationReactionStringView", "notificationType", "notificationReaction")
    fun LinearLayout.setStatusIcon(notificationStatusView: ImageView, notificationReactionImageView: ImageView, notificationReactionStringView: TextView, notificationType: String, notificationReaction: String?){
        when(notificationType){
            "reaction" -> {
                if(notificationReaction != null) ReactionViewHelper.setReaction(this.context, notificationReactionImageView, notificationReactionStringView, notificationReaction)
                notificationStatusView.visibility = View.GONE
            }
            else -> {
                notificationReactionImageView.visibility = View.GONE
                notificationReactionStringView.visibility = View.GONE
                setStatusView(notificationStatusView, notificationType)
            }
        }
    }

    fun setStatusView(statusView: ImageView, type: String){
        statusView.visibility = View.VISIBLE
        when(type){
            "follow" -> statusView.setImageResource(R.drawable.ic_follow)
            "mention" -> statusView.setImageResource(R.drawable.ic_mention)
            "reply" -> statusView.setImageResource(R.drawable.ic_reply_black_24dp)
            "renote" -> statusView.setImageResource(R.drawable.ic_re_note)
            "quote" -> statusView.setImageResource(R.drawable.ic_format_quote_black_24dp)
            "pollVote" -> statusView.setImageResource(R.drawable.ic_poll_black_24dp)
            "receiveFollowRequest" -> statusView.setImageResource(R.drawable.ic_supervisor_account_black_24dp)
        }
    }
}