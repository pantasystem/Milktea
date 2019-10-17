package jp.panta.misskeyandroidclient.view.text

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import android.view.View
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.lang.ref.WeakReference

class EmojiSpan(view: View) : ReplacementSpan(){
    val weakReference: WeakReference<View> = WeakReference(view)
    private var imageDrawable: Drawable? = null

    //これ何？



    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        return paint.measureText(text, start, end).toInt()
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

    val target = object : CustomTarget<Bitmap>(){
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val view = weakReference.get()
            if(view != null){
                imageDrawable = BitmapDrawable(view.context.resources, resource)
                view.invalidate()
            }
        }
        override fun onLoadCleared(placeholder: Drawable?) {
        }
    }
}