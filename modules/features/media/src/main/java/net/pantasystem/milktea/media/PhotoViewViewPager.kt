package net.pantasystem.milktea.media

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import kotlin.math.abs


/**
 * PhotoViewでViewPagerを利用するとエラーが発生するのでその対処をする。
 */
class PhotoViewViewPager : ViewPager {

    fun interface OnFinishEventListener {
        fun onFinish()
    }

    companion object {

        private const val SWIPE_THRESHOLD = 100

        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private var onFinishEventListener: OnFinishEventListener? = null

    fun setOnFinishEventListener(listener: OnFinishEventListener?){
        onFinishEventListener = listener
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try{
            if (ev?.action == MotionEvent.ACTION_DOWN) {
                startY = ev.y
            }
            if (ev != null) {
                return gestureDetector.onTouchEvent(ev) || super.onInterceptTouchEvent(ev)
            }
            return super.onInterceptTouchEvent(null)
        }catch(e: IllegalArgumentException){
            false
        }
    }


    private var startY = 0f

    var lastY = 0f
    var totalTranslationY = 0f


    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            totalTranslationY += e2.rawY - lastY
            viewToMove()?.translationY = totalTranslationY
            viewToMove()?.rotation = totalTranslationY / 10
            lastY = e2.rawY
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y.minus(e1.y)
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY <= 0) {
                        onSwipeTop()
                        result = true
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            if (!result) {
                viewToMove()?.animate()?.translationY(0f)?.setDuration(200)?.start()
                totalTranslationY = 0f
            }
            return result
        }

        override fun onDown(e: MotionEvent): Boolean {
            lastY = e.rawY
            return super.onDown(e)
        }


    })


    private fun onSwipeTop() {
        val currentItemView = try {
            findViewById<PhotoView>(R.id.imageView)
        } catch (e: Exception) {
            null
        }
        if (currentItemView == null || currentItemView.scale == 1.0F) {
            // Only close if not zoomed in
            onFinishEventListener?.onFinish()
//            (context as? Activity)?.finish()
        }
    }

    private fun viewToMove(): PhotoView? {
        return try {
            findViewById<PhotoView>(R.id.imageView)
        } catch (e: Exception) {
            null
        }
    }


}