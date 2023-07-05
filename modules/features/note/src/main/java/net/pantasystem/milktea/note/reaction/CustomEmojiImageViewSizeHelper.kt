@file:Suppress("UNCHECKED_CAST")

package net.pantasystem.milktea.note.reaction

import android.view.ViewGroup
import android.widget.ImageView
import kotlin.math.max

object CustomEmojiImageViewSizeHelper {

    fun<T: ViewGroup.LayoutParams> ImageView.applySizeByAspectRatio(baseHeightDp: Int, aspectRatio: Float?) {
        val metrics = resources.displayMetrics
        val heightPx = baseHeightDp * metrics.density
        applySizeByAspectRatio<T>(heightPx, aspectRatio)
    }

    fun<T: ViewGroup.LayoutParams> ImageView.applySizeByAspectRatio(baseHeightPx: Float, aspectRatio: Float?) {
        val (imageViewWidthPx, imageViewHeightPx) = calculateImageWidthAndHeightSize(baseHeightPx, aspectRatio)
        val params = layoutParams as T
        params.height = imageViewHeightPx.toInt()
        params.width = max(imageViewWidthPx, imageViewHeightPx).toInt()
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