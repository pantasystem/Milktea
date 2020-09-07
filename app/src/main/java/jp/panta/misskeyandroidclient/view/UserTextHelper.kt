package jp.panta.misskeyandroidclient.view

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator

object UserTextHelper{

    @JvmStatic
    @BindingAdapter("mainNameView", "subNameView", "user")
    fun View.setUserInfo(mainNameView: TextView?, subNameView: TextView?, user: User?){
        user?: return
        val isUserNameDefault = (this.context.applicationContext as MiApplication).getSettingStore().isUserNameDefault
        val userName: TextView?
        val name: TextView?
        if(isUserNameDefault){
            name = subNameView
            userName = mainNameView
        }else{
            name = mainNameView
            userName = subNameView
        }
        name?.let{
            name.text = CustomEmojiDecorator().decorate(user.emojis, user.getDisplayName(), name)
        }
        userName?.let{
            userName.text = user.getDisplayUserName()
        }
    }
}