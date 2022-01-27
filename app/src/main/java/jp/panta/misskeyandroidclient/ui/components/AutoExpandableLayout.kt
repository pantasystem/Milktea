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
import java.util.ArrayList

class AutoExpandableLayout : FrameLayout {

    private val limitedMaxHeight = 300
    private var expandedChangedListener: MutableList<(Boolean)->Unit> = mutableListOf()

    private var expanded = false



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
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val limitedMaxPxHeight = limitedMaxHeight * resources.displayMetrics.density



        var maxHeight = 0
        var maxWidth = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight)
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth)
            }

        }

        Log.d("AutoExpandableLayout", "maxHeight:$maxHeight")

        if (!expanded && maxHeight > limitedMaxPxHeight) {
            maxHeight = limitedMaxPxHeight.toInt()

        } else if(!expanded) {
            setExpanded(true)
        }
        Log.d("AutoExpandableLayout", "改変後maxHeight:$maxHeight")

        setMeasuredDimension(
            resolveSizeAndState(maxWidth,  widthMeasureSpec, 0),
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
    }

    fun getExpanded() : Boolean {
        return this.expanded
    }

    fun addExpandedChangedListener(listener: (Boolean)->Unit) {
        expandedChangedListener.add(listener)
    }


    companion object {


        @InverseBindingAdapter(attribute = "overflowExpanded")
        @JvmStatic fun overflowExpandedAttrChanged(viewGroup: AutoExpandableLayout): Boolean {
            return viewGroup.getExpanded()
        }

        @JvmStatic
        @BindingAdapter("overflowExpanded")
        fun bindExpanded(viewGroup: AutoExpandableLayout, expanded: Boolean?) {
            viewGroup.setExpanded(expanded ?: false)
        }


        @JvmStatic
        @BindingAdapter("overflowExpandedAttrChanged")
        fun overflowExpandedAttrChanged(viewGroup: AutoExpandableLayout, listener: InverseBindingListener?) {
            if (listener != null) {
                viewGroup.addExpandedChangedListener {
                    listener.onChange()
                }
            }
        }

    }

}