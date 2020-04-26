package jp.panta.misskeyandroidclient.view.notes

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

object StatusMessageHelper {

    @JvmStatic
    @BindingAdapter("statusMessageTargetViewNote")
    fun TextView.setStatusMessage(statusMessageTargetViewNote: PlaneNoteViewData){
        val settingStore = (context.applicationContext as MiApplication).settingStore
        val isUserNameDefault = settingStore.isUserNameDefault
        val note = statusMessageTargetViewNote.note
        val name = if(isUserNameDefault){
            note.user.getDisplayUserName()
        }else{
            note.user.getDisplayName()
        }
        val context = this.context
        val message = when{
            note.reply != null ->{
                "$name " + context.getString(R.string.replied_by)
            }
            note.reNoteId != null && note.text == null && note.files.isNullOrEmpty() ->{
                "$name " + context.getString(R.string.renoted_by)
            }
            else -> null
        }?: return
        if(isUserNameDefault){
            this.text = message
        }else{
            this.text = CustomEmojiDecorator().decorate(note.user.emojis, message, this)
        }
    }
}