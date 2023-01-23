package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import java.lang.reflect.Field

/**
 * BottomSheetDialogとViewPagerのスクロール問題を解決するためのViewPager
 * 参考
 * https://qiita.com/kafumi/items/9ca1dfefe2289d55c499
 * https://github.com/kafumi/android-bottomsheet-viewpager
 */
class BottomSheetViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    constructor(context: Context) : this(context, null)
    private val positionField: Field =
        LayoutParams::class.java.getDeclaredField("position").also {
            it.isAccessible = true
        }

    init {
        addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                requestLayout()
            }
        })
    }

    override fun getChildAt(index: Int): View {
        val stackTrace = Throwable().stackTrace
        val findScrollingChild = stackTrace.getOrNull(1)?.let {
            it.className == "com.google.android.material.bottomsheet.BottomSheetBehavior" &&
                    it.methodName == "findScrollingChild"
        }
        if (findScrollingChild != true) {
            return super.getChildAt(index)
        }

        // Swap index 0 and `currentItem`
        val currentView = getCurrentView() ?: return super.getChildAt(index)
        return if (index == 0) {
            currentView
        } else {
            var view = super.getChildAt(index)
            if (view == currentView) {
                view = super.getChildAt(0)
            }
            return view
        }
    }

    private fun getCurrentView(): View? {
        for (i in 0 until childCount) {
            val child = super.getChildAt(i)
            val lp = child.layoutParams as? LayoutParams
            if (lp != null) {
                val position = positionField.getInt(lp)
                if (!lp.isDecor && currentItem == position) {
                    return child
                }
            }
        }
        return null
    }
}