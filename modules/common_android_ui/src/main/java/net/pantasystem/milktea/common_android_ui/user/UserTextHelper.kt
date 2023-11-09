package net.pantasystem.milktea.common_android_ui.user

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.common_android_ui.DecorateTextHelper
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User

object UserTextHelper {

    @JvmStatic
    @BindingAdapter("mainNameView", "subNameView", "user", "account")
    fun View.setUserInfo(
        mainNameView: TextView?,
        subNameView: TextView?,
        user: User?,
        account: Account?,
    ) {
        val isUserNameDefault = EntryPointAccessors.fromApplication(
            this.context.applicationContext,
            BindingProvider::class.java
        )
            .settingStore()
            .isUserNameDefault
        setUserInfo(
            mainNameView = mainNameView,
            subNameView = subNameView,
            user = user,
            account = account,
            isUserNameDefault = isUserNameDefault
        )
    }

    @JvmStatic
    @BindingAdapter("mainNameView", "subNameView", "user", "account", "isUserNameDefault")
    fun View.setUserInfo(
        mainNameView: TextView?,
        subNameView: TextView?,
        user: User?,
        account: Account?,
        isUserNameDefault: Boolean = false
    ) {
        user ?: return

        val userName: TextView?
        val name: TextView?
        if (isUserNameDefault) {
            name = subNameView
            userName = mainNameView
        } else {
            name = mainNameView
            userName = subNameView
        }
        name?.let {
            DecorateTextHelper.stopDrawableAnimations(it)
            name.setText(
                CustomEmojiDecorator().decorate(
                    accountHost = account?.getHost(),
                    result = user.parsedResult,
                    name
                ),
                TextView.BufferType.SPANNABLE,
            )
        }
        userName?.let {
            userName.setText(
                user.displayUserName,
                TextView.BufferType.NORMAL
            )
        }
    }
}