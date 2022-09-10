package net.pantasystem.milktea.common_android.ui

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter

object CircleOutlineHelper {
    @JvmStatic
    @BindingAdapter("rect")
    fun ViewGroup.setCircleOutline(rect: Float?){

        outlineProvider = CircleOutlineProvider.getInstance(rect?: 20F)
    }

    @JvmStatic
    @BindingAdapter("reactByDp")
    fun ViewGroup.setCircleOutline(rect: Int?){
        rect?: return
        val r = context.resources.displayMetrics.density * rect

        outlineProvider = CircleOutlineProvider.getInstance(r)
    }

    @JvmStatic
    @BindingAdapter("rectViewById")
    fun View.setCircleOutline(rect: Int?) {
        rect?: return
        val r = context.resources.displayMetrics.density * rect
        outlineProvider = CircleOutlineProvider.getInstance(r)
    }
}