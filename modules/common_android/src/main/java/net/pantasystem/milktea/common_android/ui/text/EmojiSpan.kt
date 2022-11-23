package net.pantasystem.milktea.common_android.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.style.ReplacementSpan
import com.bumptech.glide.request.target.CustomTarget

abstract class EmojiSpan<T : Any>(val adapter: EmojiAdapter) : ReplacementSpan(){


    var imageDrawable: Drawable? = null

    private var textSize = 0


    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        //return paint.measureText(text, start, end).toInt()
        val metrics = paint.fontMetricsInt
        if(fm != null){
            fm.top = metrics.top
            fm.ascent = metrics.ascent
            fm.descent = metrics.descent
            fm.bottom = metrics.bottom
        }
        return (paint.textSize * 1.2).toInt()
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
        val emojiSize = (paint.textSize * 1.2).toInt()
        if (emojiSize != textSize) {
            textSize = emojiSize
            imageDrawable?.setBounds(0, 0, emojiSize, emojiSize)
        }
    }

    abstract
    val target: CustomTarget<T>


}