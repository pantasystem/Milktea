package net.pantasystem.milktea.media

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.github.chrisbanes.photoview.PhotoView
import kotlin.math.abs

class SwipeFinishLayout : FrameLayout {

    companion object {

        private const val SWIPE_THRESHOLD = 150

        private const val SWIPE_VELOCITY_THRESHOLD = 150
    }



    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    fun interface OnFinishEventListener {
        fun onFinish()
    }



    private var onFinishEventListener: OnFinishEventListener? = null

    fun setOnFinishEventListener(listener: OnFinishEventListener?){
        onFinishEventListener = listener
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try{
            if (ev?.action == MotionEvent.ACTION_DOWN) {
                startY = ev.y
            }
            when(ev?.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    getPhotoView()?.animate()?.translationY(0f)?.rotation(0f)?.setDuration(200)?.start()
                    totalTranslationY = 0f
                }
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
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {

            // 縦スクロールの場合
            if (abs(e2.y - startY) > abs(e2.x - (e1?.x ?: 0f))) {
                if (isNotScaled()) {
                    totalTranslationY += e2.rawY - lastY
                    viewToMove()?.translationY = totalTranslationY
                    viewToMove()?.rotation = totalTranslationY / 10
                    lastY = e2.rawY
                }

            }

            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y.minus(e1?.y ?: 0f)
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (abs(diffY) >= 0) {
                        onSwipeTop()
                        result = true
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            viewToMove()?.animate()?.translationY(0f)?.rotation(0f)?.setDuration(200)?.start()
            totalTranslationY = 0f
            return result
        }

        override fun onDown(e: MotionEvent): Boolean {
            lastY = e.rawY
            return super.onDown(e)
        }


    })


    private fun onSwipeTop() {

        if (isNotScaled()) {
            // Only close if not zoomed in
            onFinishEventListener?.onFinish()
//            (context as? Activity)?.finish()
        }
    }

    private fun getPhotoView(): PhotoView? {
        return try {
            findViewById(R.id.imageView)
        } catch (e: Exception) {
            null
        }
    }

    private fun getPlayerView(): View? {
        return try {
            findViewById(R.id.player_view)
        } catch (e: Exception) {
            null
        }
    }

    private fun viewToMove(): View? {
        return getPhotoView() ?: getPlayerView()
    }

    private fun isNotScaled(): Boolean {
        return getPhotoView() == null || getPhotoView()?.scale == 1.0F
    }


}