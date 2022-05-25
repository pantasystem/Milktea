package jp.panta.misskeyandroidclient.ui.messaging

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.ui.messaging.viewmodel.HistoryViewData

object MessagingTitleHelper {

    @JvmStatic
    @BindingAdapter("titleTargetMsgHistoryViewData")
    fun TextView.setMessageTitle(titleTargetMsgHistoryViewData: HistoryViewData){
        val isUserNameDefault = (this.context.applicationContext as MiApplication).getSettingStore().isUserNameDefault

        val title = when {
            titleTargetMsgHistoryViewData.isGroup -> {
                titleTargetMsgHistoryViewData.group?.name
            }
            isUserNameDefault -> {
                titleTargetMsgHistoryViewData.partner?.displayUserName
            }
            else -> {
                titleTargetMsgHistoryViewData.partner?.displayName
            }
        }

        if(isUserNameDefault){
            this.text = title
        }else{
            this.text = CustomEmojiDecorator().decorate(titleTargetMsgHistoryViewData.partner?.emojis, title?: "", this)
        }
    }
}