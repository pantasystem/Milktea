package net.pantasystem.milktea.common_android.ui

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_android.R
import net.pantasystem.milktea.model.setting.NoteExpandedHeightSize
import javax.inject.Inject

/**
 * ノートのコンテンツのサイズが言って以上超えた時に
 * 折りたたむためのコンポーネント
 */
@AndroidEntryPoint
class AutoCollapsingLayout : FrameLayout {


    private var limitedMaxHeight = 300
//    private var expandedChangedListener: MutableList<(Boolean) -> Unit> = mutableListOf()

    var isExpanded = false

    private var expandableButtonId: Int? = null
    private var currentAnimator: Animator? = null

    private var isNeedExpandedButtonVisible: Boolean = false

    @Inject
    internal lateinit var noteExpandedHeightSize: NoteExpandedHeightSize

    init {
        limitedMaxHeight = noteExpandedHeightSize.getNoteExpandedHeightSize()
    }


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
            attrs, R.styleable.AutoCollapsingLayout, defStyleAttr, defStyleRes
        )
        a.apply {
            val buttonId = getResourceId(R.styleable.AutoCollapsingLayout_expandableButton, -1)
            expandableButtonId = if (buttonId == -1) null else buttonId
        }

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val limitedMaxPxHeight = limitedMaxHeight * resources.displayMetrics.density
        val isExactlyHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY

        var (maxWidth, maxHeight, expandedButton) = getWidthAndHeightAndButton(
            widthMeasureSpec,
            heightMeasureSpec
        )

        if (!isExpanded && maxHeight > limitedMaxPxHeight) {
            maxHeight = limitedMaxPxHeight.toInt()
            if (expandedButton != null) {
                measureChildWithMargins(expandedButton, widthMeasureSpec, 0, heightMeasureSpec, 0)
                if (!expandedButton.isVisible) {
                    expandedButton.isVisible = true
                }
            }
            isNeedExpandedButtonVisible = true
        } else {
            isNeedExpandedButtonVisible = false
//            if (expandedButton?.isVisible == true) {
//                expandedButton.isVisible = false
//            }
        }

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
            resolveSizeAndState(
                if (isExactlyHeight) MeasureSpec.getSize(heightMeasureSpec) else maxHeight + paddingTop + paddingBottom,
                heightMeasureSpec,
                0
            )
        )
    }

    override fun addChildrenForAccessibility(outChildren: ArrayList<View>?) {
        super.addChildrenForAccessibility(outChildren)
    }

    fun findExpandButton(): View? {
        return children.firstOrNull { it.id == expandableButtonId }
    }


    fun setExpandedAndInvalidate(value: Boolean) {
        this.isExpanded = value
        isNeedExpandedButtonVisible = false

        invalidate()
        requestLayout()
//        expandedChangedListener.forEach { it.invoke(isExpanded) }
    }

    fun getWidthAndHeightAndButton(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ): Triple<Int, Int, View?> {
        var maxHeight = 0
        var maxWidth = 0

        var button: View? = null

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.id == expandableButtonId) {
                button = child
            }
            if (child.visibility != View.GONE && child.id != expandableButtonId) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight)
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth)
            }
        }
        return Triple(maxWidth, maxHeight, button)

    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(left, top, right, bottom)
    }


    fun setCurrentAnimator(animator: Animator) {
        currentAnimator?.cancel()
        currentAnimator = animator
    }


    fun setHeightAndInvalidate(pixels: Int) {
        layoutParams.height = pixels
        invalidate()
        requestLayout()
    }



    private fun layoutChildren(left: Int, top: Int, right: Int, bottom: Int) {
        val count = childCount
        val parentLeft = 0
        val parentTop = 0
        val parentRight = right - left
        val parentBottom = bottom - top

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams
                val width = child.measuredWidth
                val height = child.measuredHeight
                var childLeft: Int
                var childTop: Int

                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY
                }
                val layoutDirection = this.layoutDirection
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin

                    Gravity.RIGHT -> parentRight - width - lp.rightMargin
                    Gravity.LEFT -> parentLeft + lp.leftMargin
                    else -> parentLeft + lp.leftMargin
                }
                childTop = when (verticalGravity) {
                    Gravity.TOP -> parentTop + lp.topMargin
                    Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin

                    Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                    else -> parentTop + lp.topMargin
                }

                if (child.id == expandableButtonId) {
                    if (isNeedExpandedButtonVisible) {
                        child.layout(
                            childLeft,
                            childTop,
                            childLeft + width,
                            childTop + height
                        )
                    } else {
                        child.layout(0, 0, 0, 0)
                    }

                } else {
                    child.layout(
                        childLeft,
                        childTop,
                        childLeft + width,
                        childTop + height
                    )
                }
            }


        }
    }


    companion object {
        private const val DEFAULT_CHILD_GRAVITY = Gravity.TOP or Gravity.START


        @JvmStatic
        @BindingAdapter("overflowExpanded")
        fun bindExpanded(viewGroup: AutoCollapsingLayout, expanded: Boolean?) {
            viewGroup.setExpandedAndInvalidate(expanded ?: false)
        }
    }

}