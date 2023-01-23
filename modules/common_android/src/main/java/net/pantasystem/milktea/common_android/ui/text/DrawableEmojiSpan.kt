package net.pantasystem.milktea.common_android.ui.text


import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.penfeizhou.animation.apng.APNGDrawable

class DrawableEmojiSpan(adapter: EmojiAdapter) : EmojiSpan<Drawable>(adapter){
    //val weakReference: WeakReference<View> = WeakReference(view)



    /**
     * invalidateSelfによって呼び出されるコールバックを実装することによって
     * invalidateSelfが呼び出されたときに自信のview.invalidateを呼び出し再描画をする
     * (GifDrawableはdrawを呼び出すと自動的にcurrentのGifが読み込まれる)
     */
    inner class Animated : Drawable.Callback{
        override fun invalidateDrawable(p0: Drawable) {
            //weakReference.get()?.invalidate()
            adapter.update()
        }

        override fun scheduleDrawable(p0: Drawable, p1: Runnable, p2: Long) {
        }

        override fun unscheduleDrawable(p0: Drawable, p1: Runnable) {
        }
    }


    override val target = object : CustomTarget<Drawable>(){
        override fun onResourceReady(
            resource: Drawable,
            transition: Transition<in Drawable>?
        ) {
            imageDrawable = resource
            imageDrawable?.callback = Animated()
            when (resource) {
                is GifDrawable -> {
                    resource.start()
                }
                is APNGDrawable -> {
                    resource.start()
                }
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (resource is AnimatedImageDrawable) {
                            resource.start()
                        } else {
                            adapter.update()
                        }
                    } else {
                        adapter.update()
                    }
                }
            }
        }
        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }


}