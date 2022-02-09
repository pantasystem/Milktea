package jp.panta.misskeyandroidclient.ui.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import java.util.ArrayList

/**
 * ノートのコンテンツのサイズが言って以上超えた時に
 * 折りたたむためのコンポーネント
 */
class AutoCollapsingLayout : FrameLayout {


    private var limitedMaxHeight = 300
    private var expandedChangedListener: MutableList<(Boolean) -> Unit> = mutableListOf()

    private var expanded = false

    private var expandableButtonId: Int? = null

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
            attrs, R.styleable.AutoCollapsingLayout, defStyleAttr, defStyleRes)
        a.apply {
            val buttonId = getResourceId(R.styleable.AutoCollapsingLayout_expandableButton, -1)
            expandableButtonId = if (buttonId == -1) null else buttonId
        }

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val limitedMaxPxHeight = limitedMaxHeight * resources.displayMetrics.density

        var maxHeight = 0
        var maxWidth = 0

        var expandedButton: View? = null

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.id == expandableButtonId) {
                expandedButton = child
            }
            if (child.visibility != View.GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight)
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth)
            }

        }
        if (!expanded && maxHeight > limitedMaxPxHeight) {
            maxHeight = limitedMaxPxHeight.toInt()
            expandedButton?.visibility = View.VISIBLE
        } else {

            expandedButton?.visibility = View.GONE
            //setExpanded(true)
        }

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
            resolveSizeAndState(maxHeight + paddingTop + paddingBottom, heightMeasureSpec, 0)
        )
    }


    override fun addChildrenForAccessibility(outChildren: ArrayList<View>?) {
        super.addChildrenForAccessibility(outChildren)
    }


    fun setExpanded(value: Boolean) {
        this.expanded = value
        expandedChangedListener.forEach { it.invoke(expanded) }
        invalidate()
        Log.d("AutoExpandableLayout", "setExpanded:$value")
    }

    fun getExpanded(): Boolean {
        return this.expanded
    }

    fun addExpandedChangedListener(listener: (Boolean) -> Unit) {
        expandedChangedListener.add(listener)
    }

    fun setLimitedMaxHeight(size: Int) {
        limitedMaxHeight = size
        invalidate()
    }


    companion object {


        @InverseBindingAdapter(attribute = "overflowExpanded")
        @JvmStatic
        fun overflowExpandedAttrChanged(viewGroup: AutoCollapsingLayout): Boolean {
            return viewGroup.getExpanded()
        }

        @JvmStatic
        @BindingAdapter("overflowExpanded")
        fun bindExpanded(viewGroup: AutoCollapsingLayout, expanded: Boolean?) {
            viewGroup.setExpanded(expanded ?: false)
        }


        @JvmStatic
        @BindingAdapter("overflowExpandedAttrChanged")
        fun overflowExpandedAttrChanged(
            viewGroup: AutoCollapsingLayout,
            listener: InverseBindingListener?
        ) {
            if (listener != null) {
                viewGroup.addExpandedChangedListener {
                    listener.onChange()
                }
            }
        }

        @JvmStatic
        @BindingAdapter("limitedMaxHeight")
        fun bindLimitedMaxHeight(viewGroup: AutoCollapsingLayout, size: Int?) {
            size ?: return
            viewGroup.setLimitedMaxHeight(size)
        }


    }

}