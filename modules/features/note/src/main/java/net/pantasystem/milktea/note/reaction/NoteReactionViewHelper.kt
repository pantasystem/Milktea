package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData


object NoteReactionViewHelper {

    @JvmStatic
    @BindingAdapter("reactionTextTypeView", "reactionImageTypeView", "reaction")
    fun LinearLayout.bindReactionCount(
        reactionTextTypeView: TextView,
        reactionImageTypeView: ImageView,
        reaction: ReactionViewData,
    ) {
        val textReaction = reaction.reaction

        val emoji = reaction.emoji


        if (emoji == null) {
            reactionImageTypeView.setMemoVisibility(View.GONE)

            reactionTextTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.text = textReaction
        } else {
            reactionImageTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.setMemoVisibility(View.GONE)

            GlideApp.with(reactionImageTypeView.context)
                .load(emoji.url ?: emoji.uri)
                .let {
                    val imageAspectRatio = ImageAspectCache.get(emoji.url ?: emoji.uri)
                    if (imageAspectRatio == null) {
                        it
                    } else {
                        val metrics = context.resources.displayMetrics
                        val imageViewHeightPx = 20 * metrics.density
                        it.override((imageViewHeightPx * imageAspectRatio).toInt())
                    }
                }
//                .override(min(max(reactionImageTypeView.height, 20), 120))
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        resource ?: return false
                        val imageAspectRatio: Float = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight
                        ImageAspectCache.put(emoji.url ?: emoji.uri, imageAspectRatio)
                        return false
                    }
                })
                .into(reactionImageTypeView)
        }
    }
    

    @JvmStatic
    fun setReactionCount(
        context: Context,
        reactionTextTypeView: TextView,
        reactionImageTypeView: ImageView,
        reaction: String,
        note: PlaneNoteViewData,
    ) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            BindingProvider::class.java
        )
        val cache = entryPoint.customEmojiRepository()

        val textReaction = LegacyReaction.reactionMap[reaction] ?: reaction
        val emojiMap = cache.getAndConvertToMap(note.account.getHost())

        val r = Reaction(textReaction)
        val emoji = note.currentNote.value.emojiNameMap?.get(textReaction.replace(":", ""))
            ?: emojiMap?.get(r.getName())


        if (emoji == null) {
            reactionImageTypeView.setMemoVisibility(View.GONE)
            reactionTextTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.text = textReaction
        } else {
            reactionImageTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.setMemoVisibility(View.GONE)

            GlideApp.with(reactionImageTypeView.context)
                .load(emoji.url ?: emoji.uri)
                .into(reactionImageTypeView)
        }

    }


}