package net.pantasystem.milktea.common_android.ui

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter


object VisibilityHelper {

    @BindingAdapter("memoVisibility")
    @JvmStatic
    fun View.visibility(type: Int) {
        val currentVisibility = this.visibility
        if (type == currentVisibility) {
            return
        }
        visibility = type
    }

    @BindingAdapter("memoVisibility")
    @JvmStatic
    fun ViewGroup.visibility(type: Int) {
        val currentVisibility = this.visibility
        if (type == currentVisibility) {
            return
        }
        visibility = type
    }
}