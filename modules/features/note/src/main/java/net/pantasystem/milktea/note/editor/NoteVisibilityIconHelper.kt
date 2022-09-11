package net.pantasystem.milktea.note.editor

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.note.R

object NoteVisibilityIconHelper {

    @BindingAdapter("noteVisibility")
    @JvmStatic
    fun ImageView.setVisibilityIcon(noteVisibility: Visibility?){
        when(noteVisibility){
            is Visibility.Public -> this.setImageResource(R.drawable.ic_language_black_24dp)
            is Visibility.Home -> this.setImageResource(R.drawable.ic_home_black_24dp)
            is Visibility.Followers -> this.setImageResource(R.drawable.ic_lock_black_24dp)
            is Visibility.Specified -> this.setImageResource(R.drawable.ic_email_black_24dp)
            else -> return
        }
    }

}