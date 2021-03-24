package jp.panta.misskeyandroidclient.view.notes.editor

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.Visibility

object NoteVisibilityIconHelper {

    @BindingAdapter("noteVisibility")
    @JvmStatic
    fun ImageButton.setVisibilityIcon(noteVisibility: Visibility?){
        when(noteVisibility){
            is Visibility.Public -> this.setImageResource(R.drawable.ic_language_black_24dp)
            is Visibility.Home -> this.setImageResource(R.drawable.ic_home_black_24dp)
            is Visibility.Followers -> this.setImageResource(R.drawable.ic_lock_black_24dp)
            is Visibility.Specified -> this.setImageResource(R.drawable.ic_email_black_24dp)
            else -> return
        }
    }

}