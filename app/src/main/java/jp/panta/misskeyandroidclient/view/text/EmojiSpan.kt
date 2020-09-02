package jp.panta.misskeyandroidclient.view.text

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.style.ReplacementSpan
import android.view.View
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.lang.ref.WeakReference

class EmojiSpan(view: View) : ReplacementSpan(){
    val weakReference: WeakReference<View> = WeakReference(view)
    private var imageDrawable: Drawable? = null



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

    /**
     * invalidateSelfによって呼び出されるコールバックを実装することによって
     * invalidateSelfが呼び出されたときに自信のview.invalidateを呼び出し再描画をする
     * (GifDrawableはdrawを呼び出すと自動的にcurrentのGifが読み込まれる)
     */
    inner class Animated() : Drawable.Callback{
        override fun invalidateDrawable(p0: Drawable) {
            weakReference.get()?.invalidate()
        }

        override fun scheduleDrawable(p0: Drawable, p1: Runnable, p2: Long) {
        }

        override fun unscheduleDrawable(p0: Drawable, p1: Runnable) {
        }
    }


    val target = object : CustomTarget<Drawable>(){
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            weakReference.get()?.let{
                imageDrawable = resource
                imageDrawable?.callback = Animated()
                if(resource is GifDrawable){
                    resource.start()
                }else{
                    view.invalidate()
                }

            }
        }
        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }

    val bitmapTarget = object : CustomTarget<Bitmap>(){
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            weakReference.get()?.let{ view ->
                imageDrawable = BitmapDrawable(view.context.resources, resource)
                view.invalidate()
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }
}