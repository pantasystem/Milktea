package jp.panta.misskeyandroidclient.view.notes.editor

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask.Visibility

object NoteVisibilityIconHelper {

    @BindingAdapter("noteVisibility")
    @JvmStatic
    fun ImageButton.setVisibilityIcon(noteVisibility: Visibility?){
        when(noteVisibility){
            Visibility.PUBLIC -> this.setImageResource(R.drawable.ic_language_black_24dp)
            Visibility.HOME -> this.setImageResource(R.drawable.ic_home_black_24dp)
            Visibility.FOLLOWERS -> this.setImageResource(R.drawable.ic_lock_black_24dp)
            Visibility.SPECIFIED -> this.setImageResource(R.drawable.ic_email_black_24dp)
            else -> return
        }
    }

}