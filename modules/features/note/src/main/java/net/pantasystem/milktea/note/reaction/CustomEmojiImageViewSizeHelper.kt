@file:Suppress("UNCHECKED_CAST")

package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView

object CustomEmojiImageViewSizeHelper {

    fun<T: ViewGroup.LayoutParams> ImageView.applySizeByAspectRatio(baseHeightDp: Int, aspectRatio: Float?) {
        val (imageViewWidthPx, imageViewHeightPx) = context.calculateImageWidthAndHeightSize(baseHeightDp, aspectRatio)
        val params = layoutParams as T
        params.height = imageViewHeightPx.toInt()
        params.width = imageViewWidthPx.toInt()
        layoutParams = params
    }

    fun Context.calculateImageWidthAndHeightSize(baseHeightDp: Int, aspectRatio: Float?): Pair<Float, Float> {
        val metrics = resources.displayMetrics
        val heightPx = baseHeightDp * metrics.density
        return calculateImageWidthAndHeightSize(heightPx, aspectRatio)
    }

    fun<T: ViewGroup.LayoutParams> ImageView.applySizeByAspectRatio(baseHeightPx: Float, aspectRatio: Float?) {
        val (imageViewWidthPx, imageViewHeightPx) = calculateImageWidthAndHeightSize(baseHeightPx, aspectRatio)
        val params = layoutParams as T
        params.height = imageViewHeightPx.toInt()
        params.width = imageViewWidthPx.toInt()
        layoutParams = params
    }

    fun calculateImageWidthAndHeightSize(baseHeightPx: Float, aspectRatio: Float?): Pair<Float, Float> {
        val imageViewWidthPx = if (aspectRatio == null) {
            baseHeightPx
        } else {
            (baseHeightPx * aspectRatio)
        }
        return imageViewWidthPx to baseHeightPx
    }
}