package net.pantasystem.milktea.common_android_ui

import android.util.TypedValue
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common.ColorUtil
import net.pantasystem.milktea.common_android.R

object ViewBackgroundColorHelper {

    @BindingAdapter("setCardViewSurfaceColor")
    @JvmStatic
    fun CardView.setSurfaceColor(setCardViewSurfaceColor: Int?){
        if (setCardViewSurfaceColor != null) {
            this.setCardBackgroundColor(setCardViewSurfaceColor)
        }
        val cardView = this
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, BindingProvider::class.java)
        val store = entryPoint.colorSettingStore()
        val typedValue = TypedValue()
        cardView.context.theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        cardView.setCardBackgroundColor(ColorUtil.matchOpaqueAndColor(store.surfaceColorOpaque, typedValue.data))
    }
}