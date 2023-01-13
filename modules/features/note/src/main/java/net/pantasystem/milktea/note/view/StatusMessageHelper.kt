package net.pantasystem.milktea.note.view

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.note.viewmodel.NoteStatusMessageTextGenerator
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

        val context = this.context
        val message = NoteStatusMessageTextGenerator(note, isUserNameDefault)
        if (message == null) {
            this.visibility = View.GONE
            return
        }
        this.visibility = View.VISIBLE
        if (isUserNameDefault) {
            this.text = message.getString(context)
        } else {
            this.text = CustomEmojiDecorator().decorate(
                statusMessageTargetViewNote.account.getHost(),
                note.user.host,
                note.user.emojis,
                message.getString(context),
                this,
            )
        }
    }
}