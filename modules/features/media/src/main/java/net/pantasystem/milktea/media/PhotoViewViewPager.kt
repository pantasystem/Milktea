package net.pantasystem.milktea.media

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


/**
 * PhotoViewでViewPagerを利用するとエラーが発生するのでその対処をする。
 */
class PhotoViewViewPager : ViewPager {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)




    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try{

            return super.onInterceptTouchEvent(ev)
        }catch(e: IllegalArgumentException){
            false
        }
    }


}