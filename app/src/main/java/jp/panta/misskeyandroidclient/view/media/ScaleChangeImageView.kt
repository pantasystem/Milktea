package jp.panta.misskeyandroidclient.view.media

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

class ScaleChangeImageView : ImageView{
    companion object{
        private const val SCALE_MAX = 4.0F
        private const val SCALE_MIN = 0.5F
        private const val PINCH_SENSITIVETY = 5.0F
    }

    private var mMatrix = Matrix()
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        scaleType = ScaleType.MATRIX
        this.scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        imageMatrix = mMatrix
        scaleGestureDetector.onTouchEvent(event)
        return scaleGestureDetector.onTouchEvent(event)
    }

    private val scaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener(){
        var focusX = 0F
        var focusY = 0F

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            var scaleFactor = 1F
            val previousScale = getMatrixValue(Matrix.MSCALE_Y)

            val tmpScaleFactor = detector?.scaleFactor
            if(tmpScaleFactor != null && tmpScaleFactor >= 1.0F){
                scaleFactor = 1F + (tmpScaleFactor - 1 ) / (previousScale * PINCH_SENSITIVETY)
            }else if(tmpScaleFactor != null){
                scaleFactor = 1F - (1 - tmpScaleFactor) / (previousScale * PINCH_SENSITIVETY)
            }
            val scale = scaleFactor * previousScale

            if(scale < SCALE_MIN || scale > SCALE_MAX) {
                return false
            }

            mMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)

            invalidate()

            return super.onScale(detector)
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            if(detector != null){
                focusX = detector.focusX
                focusY = detector.focusY
            }
            return super.onScaleBegin(detector)
        }

    }

    private fun getMatrixValue(index: Int): Float{
        val values = FloatArray(9)
        imageMatrix.getValues(values)

        return values[index]
    }

}