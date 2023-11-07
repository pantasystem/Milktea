package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.common_android.ui.FontSizeUnitConverter.setMemoFontPxSize
import net.pantasystem.milktea.common_android.ui.FontSizeUnitConverter.specialPointToPixel
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.note.reaction.LegacyReaction
import net.pantasystem.milktea.model.note.reaction.Reaction
import net.pantasystem.milktea.note.reaction.CustomEmojiImageViewSizeHelper.applySizeByAspectRatio
import net.pantasystem.milktea.note.reaction.CustomEmojiImageViewSizeHelper.calculateImageWidthAndHeightSize
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData


object NoteReactionViewHelper {

//    const val REACTION_IMAGE_WIDTH_SIZE_DP = 20

    fun LinearLayout.bindReactionCount(
        reactionTextTypeView: TextView,
        reactionImageTypeView: ImageView,
        reaction: ReactionViewData,
        reactionBaseSizeSp: Float,
    ) {
        val textReaction = reaction.reaction

        val emoji = reaction.emoji

        val baseHeightPx = context.specialPointToPixel(reactionBaseSizeSp)


        if (emoji == null) {
            reactionImageTypeView.setMemoVisibility(View.GONE)
            reactionImageTypeView.setImageDrawable(null)

            reactionTextTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.text = textReaction
            reactionTextTypeView.setMemoFontPxSize(baseHeightPx)
        } else {
            reactionImageTypeView.setMemoVisibility(View.VISIBLE)
            reactionTextTypeView.setMemoVisibility(View.GONE)

            val imageAspectRatio =
                ImageAspectRatioCache.get(emoji.url ?: emoji.uri) ?: emoji.aspectRatio

            val (imageViewWidthPx, imageViewHeightPx) = calculateImageWidthAndHeightSize(
                baseHeightPx,
                imageAspectRatio
            )
            reactionImageTypeView.applySizeByAspectRatio<LinearLayout.LayoutParams>(
                baseHeightPx * 1.2f,
                imageAspectRatio
            )


            if (emoji.cachePath == null) {
                GlideApp.with(reactionImageTypeView.context)
                    .load(emoji.url ?: emoji.uri)
                    .override(imageViewWidthPx.toInt(), imageViewHeightPx.toInt())
                    .addListener(SaveImageAspectRequestListener(emoji, context))
                    .into(reactionImageTypeView)
            } else {
                GlideApp.with(reactionImageTypeView.context)
                    .load(emoji.cachePath)
                    .error(
                        GlideApp.with(reactionImageTypeView.context)
                            .load(emoji.url ?: emoji.uri)
                            .override(imageViewWidthPx.toInt(), imageViewHeightPx.toInt())
                            .addListener(SaveImageAspectRequestListener(emoji, context))
                    )
                    .override(imageViewWidthPx.toInt(), imageViewHeightPx.toInt())
                    .addListener(SaveImageAspectRequestListener(emoji, context))
                    .into(reactionImageTypeView)
            }

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
                .load(emoji.getLoadUrl())
                .error(
                    GlideApp.with(reactionImageTypeView.context)
                        .load(emoji.url ?: emoji.uri)
                )
                .into(reactionImageTypeView)
        }

    }


}