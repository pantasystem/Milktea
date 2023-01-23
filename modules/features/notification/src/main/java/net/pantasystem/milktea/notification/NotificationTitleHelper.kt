package net.pantasystem.milktea.notification

import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
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
                notification.user?.displayUserName ?: ""
            } else {
                val account = EntryPointAccessors.fromApplication(
                    this.context.applicationContext,
                    BindingProvider::class.java
                ).accountStore().currentAccount
                CustomEmojiDecorator().decorate(
                    account?.getHost(),
                    notification.user?.parsedResult ?: CustomEmojiParsedResult("", emptyList()),
                    this
                )

            }
        }
    }
}