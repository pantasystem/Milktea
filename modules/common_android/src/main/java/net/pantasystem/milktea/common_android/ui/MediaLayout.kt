package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.core.view.isVisible

class MediaLayout : FrameLayout {

    private val spaceMargin = 8
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
//        val a = context.obtainStyledAttributes(
//            attrs, R.styleable.AutoCollapsingLayout, defStyleAttr, defStyleRes
//        )
//        a.apply {
//            val buttonId = getResourceId(R.styleable.AutoCollapsingLayout_expandableButton, -1)
//            expandableButtonId = if (buttonId == -1) null else buttonId
//        }
//
//        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val childOne: View? = getChildAt(0)
        val childTwo: View? = getChildAt(1)
        val childThree: View? = getChildAt(2)

        val visibleChildItemCount = children.count { it.isVisible }
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width * 10.0 / 16.0

        when(visibleChildItemCount) {
            0 -> {}
            1 -> {
                childOne?.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
                )
            }
            2 -> {
                // widthを2分割したサイズ
                val childWidth = width / 2
                // heightは親と同じサイズ
                childOne?.measure(
                    MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
                )
                childTwo?.measure(
                    MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
                )
            }
            3 -> {
                val childWidth = width / 2
                val childHeight = height / 2
                childOne?.measure(
                    MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height.toInt(), MeasureSpec.EXACTLY)
                )
                childTwo?.measure(
                    MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight.toInt() + spaceMargin, MeasureSpec.EXACTLY)
                )
                childThree?.measure(
                    MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(childHeight.toInt() + spaceMargin, MeasureSpec.EXACTLY)
                )
            }
            4 -> {
                val childWidth = width / 2
                val childHeight = height / 2
                children.forEach{ view ->
                    view.measure(
                        MeasureSpec.makeMeasureSpec(childWidth + spaceMargin, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight.toInt() + spaceMargin, MeasureSpec.EXACTLY)
                    )
                }
            }
        }
        setMeasuredDimension(width, height.toInt())
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val visibleChildItemCount = children.count { it.isVisible }
        val width = right - left
        val height = width * 10.0 / 16.0
        when(visibleChildItemCount) {
            0 -> {}
            1 -> {
                val childOne = getChildAt(0)
                childOne.layout(0, 0, width, height.toInt())
            }
            2 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childWidth = width / 2
                childOne.layout(0, 0, childWidth - spaceMargin, height.toInt())
                childTwo.layout(childWidth + spaceMargin, 0, width, height.toInt())
            }
            3 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childThree = getChildAt(2)
                val childWidth = width / 2
                val childHeight = height / 2
                childOne.layout(0, 0, childWidth - spaceMargin, height.toInt())
                childTwo.layout(childWidth + spaceMargin, 0, width, childHeight.toInt() - spaceMargin)
                childThree.layout(childWidth + spaceMargin, childHeight.toInt() + spaceMargin, width, height.toInt())
            }
            4 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childThree = getChildAt(2)
                val childFour = getChildAt(3)
                val childWidth = width / 2
                val childHeight = height / 2
                childOne.layout(0, 0, childWidth - spaceMargin, childHeight.toInt() - spaceMargin)
                childTwo.layout(childWidth + spaceMargin, 0, width, childHeight.toInt() - spaceMargin)
                childThree.layout(0, childHeight.toInt() + spaceMargin, childWidth - spaceMargin, height.toInt())
                childFour.layout(childWidth + spaceMargin, childHeight.toInt() + spaceMargin, width, height.toInt())
            }
        }
    }



}