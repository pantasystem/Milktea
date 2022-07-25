package jp.panta.misskeyandroidclient.ui.notes.view

import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

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
