package net.pantasystem.milktea.note.view

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android.resource.getString
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_android.ui.text.EmojiAdapter
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NoteStatusMessageTextGenerator
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import kotlin.math.min


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
        val text = if (isUserNameDefault) {
            message.getString(context)
        } else {
            CustomEmojiDecorator().decorate(
                statusMessageTargetViewNote.account.getHost(),
                note.user.host,
                note.user.emojis,
                message.getString(context),
                this,
            )
        }

        val icon = when {
            note.reply != null -> R.drawable.ic_reply_black_24dp
            note.renote != null -> R.drawable.ic_re_note
            else -> null
        }

        this.text = if (icon != null) {
            SpannableStringBuilder(text).apply {
                insert(0, "icon")
                val span = DrawableEmojiSpan(EmojiAdapter(this@setStatusMessage), icon)
                val drawable = ContextCompat.getDrawable(context, icon)
                drawable?.setTint(currentTextColor)
                Glide.with(context)
                    .load(drawable)
                    .override(min(textSize.toInt(), 640))
                    .into(span.target)

                setSpan(span, 0, "icon".length, 0)
            }
        } else {
            text
        }
    }
}