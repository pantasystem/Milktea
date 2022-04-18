package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.notes.Visibility

object NoteVisibilityIconHelper {

    @BindingAdapter("noteVisibility")
    @JvmStatic
    fun ImageView.setVisibilityIcon(noteVisibility: net.pantasystem.milktea.model.notes.Visibility?){
        when(noteVisibility){
            is net.pantasystem.milktea.model.notes.Visibility.Public -> this.setImageResource(R.drawable.ic_language_black_24dp)
            is net.pantasystem.milktea.model.notes.Visibility.Home -> this.setImageResource(R.drawable.ic_home_black_24dp)
            is net.pantasystem.milktea.model.notes.Visibility.Followers -> this.setImageResource(R.drawable.ic_lock_black_24dp)
            is net.pantasystem.milktea.model.notes.Visibility.Specified -> this.setImageResource(R.drawable.ic_email_black_24dp)
            else -> return
        }
    }

}