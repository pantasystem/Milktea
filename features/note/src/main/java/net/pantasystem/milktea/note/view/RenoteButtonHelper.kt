package net.pantasystem.milktea.note.view

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

object RenoteButtonHelper {

    @JvmStatic
    @BindingAdapter("notesViewModelForClickRenote", "clickTargetNote")
    fun ImageButton.renoteButtonClickHelper(notesViewModelForClickRenote: NoteCardActionListenerAdapter?, clickTargetNote: PlaneNoteViewData?) {
        if(notesViewModelForClickRenote == null || clickTargetNote == null) {
            return
        }
        this.setOnClickListener {
            notesViewModelForClickRenote.onRenoteButtonClicked(clickTargetNote)
        }
        this.setOnLongClickListener {
            notesViewModelForClickRenote.onRenoteButtonLongClicked(clickTargetNote)
            false
        }
    }
}
