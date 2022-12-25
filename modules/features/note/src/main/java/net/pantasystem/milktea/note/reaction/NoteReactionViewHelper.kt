package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

object NoteReactionViewHelper {

    @JvmStatic
    @BindingAdapter("reactionTextTypeView", "reactionImageTypeView", "reaction", "note")
    fun LinearLayout.setReactionCount(
        reactionTextTypeView: TextView,
        reactionImageTypeView: ImageView,
        reaction: String,
        note: PlaneNoteViewData
    ) {
        setReactionCount(this.context, reactionTextTypeView, reactionImageTypeView, reaction, note)
    }

    @JvmStatic
    fun setReactionCount(
        context: Context,
        reactionTextTypeView: TextView,
        reactionImageTypeView: ImageView,
        reaction: String,
        note: PlaneNoteViewData
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BindingProvider::class.java
        )
        val cache = entryPoint.metaRepository()

        val textReaction = LegacyReaction.reactionMap[reaction] ?: reaction
        val metaEmojis = cache.get(note.account.normalizedInstanceDomain)?.emojis ?: emptyList()
        val emoji = note.emojiMap[textReaction.replace(":", "")] ?: metaEmojis.firstOrNull {
            textReaction.replace(":", "") == it.name
        }

        if (emoji == null) {
            reactionImageTypeView.visibility = View.GONE
            reactionTextTypeView.visibility = View.VISIBLE
            reactionTextTypeView.text = textReaction
        } else {
            reactionImageTypeView.visibility = View.VISIBLE
            reactionTextTypeView.visibility = View.GONE

            if (emoji.type?.contains("svg") == true || emoji.url?.contains("svg") == true || emoji.uri?.contains(
                    "svg"
                ) == true
            ) {

                GlideApp.with(context)
                    .load(emoji.url ?: emoji.uri)
                    .fitCenter()
                    .into(reactionImageTypeView)
            } else {
                GlideApp.with(reactionImageTypeView.context)
                    .load(emoji.url ?: emoji.uri)
                    .fitCenter()
                    .into(reactionImageTypeView)
            }
        }

    }
}