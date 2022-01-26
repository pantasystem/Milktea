package jp.panta.misskeyandroidclient.ui.text

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import com.bumptech.glide.request.target.CustomTarget

abstract class EmojiSpan<T : Any>(val adapter: EmojiAdapter) : ReplacementSpan(){


    var imageDrawable: Drawable? = null



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

        val emojiSize = (paint.textSize * 1.1).toInt()
        drawable.setBounds(0, 0, emojiSize, emojiSize)

        var transY = (bottom - drawable.bounds.bottom).toFloat()
        transY -= paint.fontMetrics.descent / 2
        canvas.translate(x, transY)
        drawable.draw(canvas)
        canvas.restore()
    }




    abstract
    val target: CustomTarget<T>


}