package jp.panta.misskeyandroidclient.view.notification

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.UnknownNotification

object NotificationHelper {

    @JvmStatic
    @BindingAdapter("notification")
    fun TextView.setUnknownNotificationMessage(n: Notification) {

        if(n is UnknownNotification) {
            this.text = context.getString(R.string.unknown_notification_msg, n.id.notificationId, n.rawType, n.userId.id)
        }
    }
}