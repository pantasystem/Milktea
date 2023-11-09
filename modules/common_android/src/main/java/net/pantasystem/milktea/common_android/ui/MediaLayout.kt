package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.core.view.isVisible
import net.pantasystem.milktea.common_android.R
import kotlin.math.max
import kotlin.math.min

/**
 * タイムラインの投稿のメディアを配置するためのレイアウト
 */
class MediaLayout : ViewGroup {

    private var spaceMargin = 8
    private var _visibleChildItemCount = 0
    private var _visibleChildren = listOf<View>()
    private var _isOddVisibleItemCount = false

    private var _height: Int = 0
    private var _rightElHeight: Double = 0.0
    private var _leftElHeight: Double = 0.0
    private var _colCount: Int = 0
    private var _childWidth: Int = 0

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
        _visibleChildren = children.filter { it.isVisible }.toList()
        _visibleChildItemCount = _visibleChildren.size
        _isOddVisibleItemCount = _visibleChildItemCount % 2 == 1
        if (_visibleChildItemCount == 0) {
            _height = 0
            setMeasuredDimension(0, 0)
            return
        }

        // 2列以上表示したくない
        _colCount = min(_visibleChildItemCount, 2)

        val leftElCount = if (_colCount == 0) 0 else max(_visibleChildItemCount / _colCount, 1)
        val rightElCount = _visibleChildItemCount - leftElCount

        val width = MeasureSpec.getSize(widthMeasureSpec)
//        val height = width * 10.0 / 16.0

        _childWidth = width / max(_colCount, 1)
        val aspectRatio = 16.0 / 10.0
        val minChildHeight = if (_visibleChildItemCount == 2) width / aspectRatio else _childWidth / aspectRatio
        val height = minChildHeight * max(rightElCount, leftElCount)

        _rightElHeight = if (rightElCount == 0) 0.0 else height / rightElCount
        _leftElHeight = if (leftElCount == 0) 0.0 else height / leftElCount

        for (i in 0 until _visibleChildItemCount) {
            val child = _visibleChildren[i]
            val isRight = i % 2 == 1
            val childHeight = if (isRight) {
                _rightElHeight
            } else {
                _leftElHeight
            }
            child.measure(
                MeasureSpec.makeMeasureSpec(_childWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight.toInt(), MeasureSpec.EXACTLY)
            )
        }


        _height = height.toInt()
        setMeasuredDimension(width, height.toInt())
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val width = right - left

        for (i in 0 until _visibleChildItemCount) {
            val child = _visibleChildren[i]

            val isOddView = i % 2 == 1
            val isLast = i == _visibleChildItemCount - 1
            val isRight = if (_isOddVisibleItemCount) {
                _visibleChildItemCount > 1 && (isLast || isOddView)
            } else {
                i % 2 == 1
            }
            val childHeight = if (isRight) {
                _rightElHeight
            } else {
                _leftElHeight
            }
            val childTop = (i / 2) * childHeight
            val childLeft = if (isRight) {
                width - _childWidth
            } else {
                0
            }

            val childBottom = childTop + childHeight
            val childRight = childLeft + _childWidth

            val hasRightItem = !isOddView && !isLast && _visibleChildItemCount > 1
            val hasTopItem = i >= 2
            val hasBottomItem = if (_visibleChildItemCount > 3) {
                if (_isOddVisibleItemCount) {
                    // 最後ではないこと, 最後から二つ目ではないこと
                    !isLast && i != _visibleChildItemCount - 3
                } else {
                    // 最後から二つ目より前であること
                    i < _visibleChildItemCount - 2
                }
            } else {
                // 3つ以下の場合は、要素数が3かつ2番目の要素であること
                _visibleChildItemCount == 3 && i == 1
            }
            val childTopMargin = if (hasTopItem) +spaceMargin else 0
            val childBottomMargin = if (hasBottomItem) -spaceMargin else 0
            val childLeftMargin = if (isRight) +spaceMargin else 0
            val childRightMargin = if (hasRightItem) -spaceMargin else 0

            child.layout(
                childLeft + childLeftMargin,
                childTop.toInt() + childTopMargin,
                childRight + childRightMargin,
                childBottom.toInt() + childBottomMargin
            )
        }

    }


}