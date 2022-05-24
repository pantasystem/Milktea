package jp.panta.misskeyandroidclient.ui.components

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R

/**
 * ノートのコンテンツのサイズが言って以上超えた時に
 * 折りたたむためのコンポーネント
 */
class AutoCollapsingLayout : FrameLayout {


    private var limitedMaxHeight = 300
    private var expandedChangedListener: MutableList<(Boolean) -> Unit> = mutableListOf()

    var isExpanded = false

    private var expandableButtonId: Int? = null
    private var currentAnimator: Animator? = null

    init {
        limitedMaxHeight =
            (context.applicationContext as MiApplication).getSettingStore().noteExpandedHeightSize
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
                expandedButton.isVisible = true
            }
        } else {
            expandedButton?.isVisible = false
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

        invalidate()
        requestLayout()
        expandedChangedListener.forEach { it.invoke(isExpanded) }
    }

    private fun getWidthAndHeightAndButton(
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


    fun setCurrentAnimator(animator: Animator) {
        currentAnimator?.cancel()
        currentAnimator = animator
    }


    fun setHeightAndInvalidate(pixels: Int) {
        layoutParams.height = pixels
        invalidate()
        requestLayout()
    }


    companion object {


        @JvmStatic
        @BindingAdapter("overflowExpanded")
        fun bindExpanded(viewGroup: AutoCollapsingLayout, expanded: Boolean?) {
            viewGroup.setExpandedAndInvalidate(expanded ?: false)
        }


        @JvmStatic
        @BindingAdapter("targetButton", "onExpandedChanged")
        fun AutoCollapsingLayout.setExpandedWithAnimation(
            targetButton: View,
            onExpandedChanged: (() -> Unit)?
        ) {

            val button = findExpandButton()
            button?.alpha = 1.0f

            targetButton.setOnClickListener {

                val beforeHeight = measuredHeight

                val (_, maxHeight, _) = getWidthAndHeightAndButton(
                    MeasureSpec.makeMeasureSpec(
                        0,
                        MeasureSpec.UNSPECIFIED
                    ), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )

                val animator =
                    ValueAnimator.ofInt(beforeHeight, maxHeight)
                        .setDuration(100).apply {
                            addUpdateListener {
                                onExpandedChanged?.invoke()
                                val newHeight = it.animatedValue as Int
                                setHeightAndInvalidate(newHeight)
                                button?.alpha = 1f - newHeight.toFloat() / maxHeight.toFloat()
                            }


                            doOnEnd {
                                isExpanded = true
                                findExpandButton()?.isVisible = false
                                onExpandedChanged?.invoke()
                                setHeightAndInvalidate(ViewGroup.LayoutParams.WRAP_CONTENT)
                            }
                            doOnCancel {
                                setHeightAndInvalidate(ViewGroup.LayoutParams.WRAP_CONTENT)
                            }
                        }


                setCurrentAnimator(animator)
                animator.start()
            }

        }

    }

}