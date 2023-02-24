package net.pantasystem.milktea.common_android.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import java.lang.ref.WeakReference
import kotlin.math.min

abstract class EmojiSpan<T: Any?>(val key: T) : ReplacementSpan(){

    companion object {
        private val drawableSizeCache = mutableMapOf<Any, EmojiSizeCache>()
    }

    private var _imageDrawable: WeakReference<Drawable?>? = null
    var imageDrawable: Drawable?
        get() = _imageDrawable?.get()
        set(value) {
            _imageDrawable = WeakReference(value)
        }


    /**
     * imageDrawableにDrawableが代入されている時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される。
     * 画像は縦横比が異なることがあるので、それぞれの高さが代入される。
     */
    private var textHeight: Int = 0
    private var textWidth: Int = 0
    private var isSizeComputed = false

    /**
     * imageDrawableがnullの時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される
     * またDrawableがNullの段階の時にupdateImageDrawableSizeが呼び出された時は画像が正方形として扱われる。
     */
    private var beforeTextSize: Int = 0


    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val drawable = imageDrawable
        val size = key?.let {
            drawableSizeCache[key]
        } ?: drawable?.let {
            EmojiSizeCache(
                intrinsicHeight = it.intrinsicHeight,
                intrinsicWidth = it.intrinsicWidth
            )
        }
        key?.run {
            drawableSizeCache[key] ?: drawable?.let {
                EmojiSizeCache(
                    intrinsicHeight = it.intrinsicHeight,
                    intrinsicWidth = it.intrinsicWidth
                )
            }
        }
        val metrics = paint.fontMetricsInt
        if (fm != null) {
            fm.top = metrics.top
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.bottom = metrics.bottom
        }

        if (size == null || beforeTextSize != 0) {
            beforeTextSize = (paint.textSize * 1.2).toInt()
            return beforeTextSize
        }
        key?.run {
            drawableSizeCache[key] = size
        }

        beforeTextSize = 0

        val textHeight = paint.textSize
        val imageWidth = size.intrinsicWidth
        val imageHeight = size.intrinsicHeight

        // 画像がテキストの高さよりも大きい場合、画像をテキストと同じ高さに縮小する
        val scale = if (imageHeight > textHeight) {
            textHeight / imageHeight.toFloat()
        } else {
            1.0f
        }

        // テキストの高さに合わせた画像の幅
        return (imageWidth * scale).toInt()
    }

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        updateImageDrawableSize(ds)
    }


    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = imageDrawable
        drawable?: return

        canvas.save()
        updateImageDrawableSize(paint)
        var transY = (bottom - drawable.bounds.bottom).toFloat()
        transY -= paint.fontMetrics.descent / 2
        canvas.translate(x, transY)
        drawable.draw(canvas)
        canvas.restore()

    }


    private fun updateImageDrawableSize(paint: Paint) {
        val drawable = imageDrawable
        val size = key?.let {
            drawableSizeCache[key]
        } ?: drawable?.let {
            EmojiSizeCache(
                intrinsicWidth = it.intrinsicWidth,
                intrinsicHeight = it.intrinsicHeight
            )
        } ?: return
        key?.run {
            drawableSizeCache[key] = size
        }
        val imageWidth = size.intrinsicWidth
        val imageHeight = size.intrinsicHeight
        val emojiHeight = min((paint.textSize).toInt(), 640)

        val unknownEmojiSize = imageWidth <= 0 || imageHeight <= 0
        if (beforeTextSize != 0 && beforeTextSize != emojiHeight || unknownEmojiSize) {
            if (!isSizeComputed) {
                beforeTextSize = emojiHeight
                imageDrawable?.setBounds(0, 0, emojiHeight, emojiHeight)
                isSizeComputed = true
            }
            return
        }

        val ratio = imageWidth.toFloat() / imageHeight.toFloat()

        val scaledImageWidth = (emojiHeight * ratio).toInt()

        if (!isSizeComputed) {
            textHeight = emojiHeight
            textWidth = scaledImageWidth
            isSizeComputed = true
            imageDrawable?.setBounds(0, 0, scaledImageWidth, emojiHeight)
        }
    }

}

data class EmojiSizeCache(
    val intrinsicWidth: Int,
    val intrinsicHeight: Int,
)