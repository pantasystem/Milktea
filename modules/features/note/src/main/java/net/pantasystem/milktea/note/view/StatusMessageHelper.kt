package net.pantasystem.milktea.note.view

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData


object StatusMessageHelper {

    @JvmStatic
    @BindingAdapter("statusMessageTargetViewNote")
    fun TextView.setStatusMessage(statusMessageTargetViewNote: PlaneNoteViewData) {
        val entrypoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BindingProvider::class.java
        )
        val settingStore = entrypoint.settingStore()

        val isUserNameDefault = settingStore.isUserNameDefault
        val note = statusMessageTargetViewNote.note
        val name = if (isUserNameDefault) {
            note.user.displayUserName
        } else {
            note.user.displayName
        }
        val context = this.context
        val message = when {
            note.reply != null -> {
                context.getString(R.string.replied_by, name)
            }
            note.note.isRenote() && !note.note.hasContent() -> {
                context.getString(R.string.renoted_by, name)
            }
            note is NoteRelation.Featured -> {
                context.getString(R.string.featured)
            }
            note is NoteRelation.Promotion -> {
                context.getString(R.string.promotion)
            }

            else -> null
        }
        if (message == null) {
            this.visibility = View.GONE
            return
        }
        this.visibility = View.VISIBLE
        if (isUserNameDefault) {
            this.text = message
        } else {
            this.text = CustomEmojiDecorator().decorate(
                statusMessageTargetViewNote.account.getHost(),
                note.user.host,
                note.user.emojis,
                message,
                this,
            )
        }
    }
}