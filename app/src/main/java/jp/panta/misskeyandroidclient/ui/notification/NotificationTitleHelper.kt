package jp.panta.misskeyandroidclient.ui.notification

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.PollEndedNotification
import jp.panta.misskeyandroidclient.viewmodel.MiCore

object NotificationTitleHelper {

    @JvmStatic
    @BindingAdapter("notificationTitle")
    fun TextView.setNotificationTitle(notification: NotificationRelation) {
        val miCore = this.context.applicationContext as MiCore
        this.text = when (notification.notification) {
            is PollEndedNotification -> {
                context.getString(R.string.poll_ended)
            }
            else -> if (miCore.getSettingStore().isUserNameDefault) {
                notification.user?.displayUserName?: ""
            } else {
                notification.user?.displayName?: ""
            }
        }
    }
}