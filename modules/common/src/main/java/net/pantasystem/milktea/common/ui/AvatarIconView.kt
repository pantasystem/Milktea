package net.pantasystem.milktea.common.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common.R
import net.pantasystem.milktea.common.glide.GlideApp

class AvatarIconView : AppCompatImageView {

    companion object {
        const val SHAPE_CIRCLE = 0
        const val SHAPE_ROUNDED_CORNER = 1

        @BindingAdapter("imageUrl")
        @JvmStatic
        fun AvatarIconView.setImageUrl(url: String?) {
            GlideApp.with(this.context)
                .load(url)
                .error(R.drawable.ic_cloud_off_black_24dp)
                .into(this)
        }

        @BindingAdapter("shape")
        @JvmStatic
        fun AvatarIconView.setShape(shape: Int) {
            setIconShape(shape)
        }
    }

    constructor(context: Context) : super(context) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.AvatarIconView, 0, 0).apply {
            try {
                setIconShape(getInteger(R.styleable.AvatarIconView_iconShape, 0))
            } finally {
                recycle()
            }
        }
    }


    fun setIconShape(shape: Int) {
        when(shape) {
            SHAPE_CIRCLE -> {
                if (outlineProvider !is CircleOutlineProvider) {
                    outlineProvider = CircleOutlineProvider
                    clipToOutline = true
                }
            }
            SHAPE_ROUNDED_CORNER -> {
                if (outlineProvider !is RoundedCornerShapeProvider) {
                    outlineProvider = RoundedCornerShapeProvider
                    clipToOutline = true
                }
            }
        }
    }


}