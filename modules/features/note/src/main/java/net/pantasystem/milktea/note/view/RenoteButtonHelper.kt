package net.pantasystem.milktea.note.view

import android.content.res.ColorStateList
import android.util.TypedValue
import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
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

    @JvmStatic
    @BindingAdapter("renoteButtonColor")
    fun ImageButton.setRenoteButtonIconFromState(note: Note?) {
        note ?: return
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.normalIconTint, typedValue, true)
        val normalTintColor = typedValue.data

        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        when(val type = note.type) {
            is Note.Type.Mastodon -> {
                if (type.reblogged == true) {
                    this.imageTintList = ColorStateList.valueOf(primaryColor)
                } else {
                    this.imageTintList = ColorStateList.valueOf(normalTintColor)
                }
            }
            Note.Type.Misskey -> {
                this.imageTintList = ColorStateList.valueOf(normalTintColor)
            }
        }
    }
}
