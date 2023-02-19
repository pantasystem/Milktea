package net.pantasystem.milktea.common_android.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import kotlin.math.min

abstract class EmojiSpan<T : Any> : ReplacementSpan(){


    var imageDrawable: Drawable? = null


    /**
     * imageDrawableにDrawableが代入されている時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される。
     * 画像は縦横比が異なることがあるので、それぞれの高さが代入される。
     */
    private var textHeight: Int = 0
    private var textWidth: Int = 0

    /**
     * imageDrawableがnullの時にupdateImageDrawableSizeが呼び出されるとここに絵文字のサイズが代入される
     * またDrawableがNullの段階の時にupdateImageDrawableSizeが呼び出された時は画像が正方形として扱われる。
     */
    private var beforeTextSize: Int = 0


    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val drawable = imageDrawable
        if (drawable == null) {
            beforeTextSize = (paint.textSize * 1.2).toInt()
            return beforeTextSize
        }
        beforeTextSize = 0

        val textHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        // 画像がテキストの高さよりも大きい場合、画像をテキストと同じ高さに縮小する
        val ratio = if (imageHeight > textHeight) {
            textHeight.toFloat() / imageHeight.toFloat()
        } else {
            1.0f
        }

        val metrics = paint.fontMetricsInt
        if(fm != null){
            fm.top = metrics.top
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.bottom = metrics.bottom
        }

        val scaledImageWidth = (imageWidth * ratio).toInt()

        // テキストの高さに合わせた画像の幅
        val availableWidth = paint.measureText(text, start, end)
        return if (scaledImageWidth > availableWidth) {
            (availableWidth / ratio).toInt()
        } else {
            scaledImageWidth
        }
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
        val drawable = imageDrawable ?: return
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val emojiHeight = min((paint.textSize).toInt(), 640)

        if (imageWidth <= 0 || imageHeight <= 0) {
            if (beforeTextSize != 0 && beforeTextSize != emojiHeight) {
                beforeTextSize = emojiHeight
                imageDrawable?.setBounds(0, 0, emojiHeight, emojiHeight)
                return
            }
            return
        }

        if (beforeTextSize != 0 && beforeTextSize != emojiHeight) {
            beforeTextSize = emojiHeight
            imageDrawable?.setBounds(0, 0, emojiHeight, emojiHeight)
            return
        }

        val ratio = imageWidth.toFloat() / imageHeight.toFloat()

        val width = (emojiHeight * ratio).toInt()


        if (width != textWidth || emojiHeight != textHeight) {
            textHeight = emojiHeight
            textWidth = width
            imageDrawable?.setBounds(0, 0, width, emojiHeight)
        }
    }



}