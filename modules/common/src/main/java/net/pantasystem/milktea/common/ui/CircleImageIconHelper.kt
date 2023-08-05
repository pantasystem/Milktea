package net.pantasystem.milktea.common.ui

import android.graphics.Outline
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import net.pantasystem.milktea.common.R

object CircleImageIconHelper {

    @BindingAdapter("circleIcon")
    @JvmStatic
    fun ImageView.setCircleIcon(url: String?){

        Glide.with(this.context)
            .load(url)
            .error(R.drawable.ic_cloud_off_black_24dp)
            .into(this)

        if (outlineProvider !is CircleOutlineProvider) {
            outlineProvider = CircleOutlineProvider
            clipToOutline = true
        }
    }
}


object CircleOutlineProvider : ViewOutlineProvider() {

    override fun getOutline(view: View?, outline: Outline?) {
        view ?: return
        outline ?: return
        outline.setOval(0, 0, view.width, view.height)
    }
}

object RoundedCornerShapeProvider : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        view ?: return
        outline ?: return
        val left = 0
        val top = 0
        val right = view.width
        val bottom = view.height
        val cornerRadiusDP = 8f
        val cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerRadiusDP, view.context.resources.displayMetrics).toInt()

        // all corners
        outline.setRoundRect(left, top, right, bottom, cornerRadius.toFloat())
    }
}