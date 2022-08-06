package jp.panta.misskeyandroidclient.ui.notification

import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.BindingProvider
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.notification.PollEndedNotification


object NotificationTitleHelper {

    @JvmStatic
    @BindingAdapter("notificationTitle")
    fun TextView.setNotificationTitle(notification: NotificationRelation) {
        val isUserNameDefault = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BindingProvider::class.java
        ).settingStore()
            .isUserNameDefault
        this.text = when (notification.notification) {
            is PollEndedNotification -> {
                context.getString(R.string.poll_ended)
            }
            else -> if (isUserNameDefault) {
                notification.user?.displayUserName?: ""
            } else {
                notification.user?.displayName?: ""
            }
        }
    }
}