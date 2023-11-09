package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.core.view.isVisible
import net.pantasystem.milktea.common_android.R
import kotlin.math.max
import kotlin.math.min

class MediaLayout : ViewGroup {

    private var spaceMargin = 8
    private var visibleChildItemCount = 0

    private var _height: Int = 0

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
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.MediaLayout, defStyleAttr, defStyleRes
        )
        a.apply {
            val spaceSize = getResourceId(R.styleable.MediaLayout_spaceSize, 8)
            spaceMargin = if (spaceSize != 0) spaceSize / 2 else 0
        }

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val visibleViews = children.filter { it.isVisible }.toList()
        visibleChildItemCount = visibleViews.size
        if (visibleChildItemCount == 0) {
            _height = 0
            setMeasuredDimension(0, 0)
            return
        }

        // 2列以上表示したくない
        val colCount = min(visibleChildItemCount, 2)

        val leftElCount = if (colCount == 0) 0 else max(visibleChildItemCount / colCount, 1)
        val rightElCount = visibleChildItemCount - leftElCount

        val width = MeasureSpec.getSize(widthMeasureSpec)
//        val height = width * 10.0 / 16.0

        val childWidth = width / max(colCount, 1)
        val aspectRatio = 16.0 / 10.0
        val minChildHeight = if (visibleChildItemCount == 2) width / aspectRatio else childWidth / aspectRatio
        val height = minChildHeight * max(rightElCount, leftElCount)

        val rightElHeight = if (rightElCount == 0) 0.0 else height / rightElCount
        val leftElHeight = if (leftElCount == 0) 0.0 else height / leftElCount

        for (i in 0 until visibleChildItemCount) {
            val child = visibleViews[i]
            val isRight = i % 2 == 1
            val childHeight = if (isRight) {
                rightElHeight
            } else {
                leftElHeight
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight.toInt(), MeasureSpec.EXACTLY)
            )
        }


        _height = height.toInt()
        setMeasuredDimension(width, height.toInt())
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left
        val height = _height
        when (visibleChildItemCount) {
            0 -> {}
            1 -> {
                val childOne = getChildAt(0)
                childOne.layout(0, 0, width, height)
            }

            2 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childWidth = width / 2
                childOne.layout(0, 0, childWidth - spaceMargin, height)
                childTwo.layout(childWidth + spaceMargin, 0, width, height)
            }

            3 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childThree = getChildAt(2)
                val childWidth = width / 2
                val childHeight = height / 2
                childOne.layout(0, 0, childWidth - spaceMargin, height)
                childTwo.layout(childWidth + spaceMargin, 0, width, childHeight - spaceMargin)
                childThree.layout(
                    childWidth + spaceMargin, childHeight + spaceMargin, width,
                    height
                )
            }

            4 -> {
                val childOne = getChildAt(0)
                val childTwo = getChildAt(1)
                val childThree = getChildAt(2)
                val childFour = getChildAt(3)
                val childWidth = width / 2
                val childHeight = height / 2
                childOne.layout(0, 0, childWidth - spaceMargin, childHeight - spaceMargin)
                childTwo.layout(childWidth + spaceMargin, 0, width, childHeight - spaceMargin)
                childThree.layout(/* l = */ 0, /* t = */
                    childHeight + spaceMargin, /* r = */
                    childWidth - spaceMargin, /* b = */
                    height
                )
                childFour.layout(
                    childWidth + spaceMargin, childHeight + spaceMargin, width,
                    height
                )
            }
        }
    }


}