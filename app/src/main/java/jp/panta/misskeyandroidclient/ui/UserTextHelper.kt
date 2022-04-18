package jp.panta.misskeyandroidclient.ui

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.MiApplication
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiDecorator

object UserTextHelper{

    @JvmStatic
    @BindingAdapter("mainNameView", "subNameView", "user")
    fun View.setUserInfo(mainNameView: TextView?, subNameView: TextView?, user: net.pantasystem.milktea.model.user.User?){
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