package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.TypedValue
import android.widget.TextView

object FontSizeHelper {
    fun TextView.setMemoFontPxSize(fontSize: Float) {
        if (this.textSize == fontSize) {
            return
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    fun TextView.setMemoFontSpSize(fontSize: Float) {
        val baseHeightPx = context.resources.displayMetrics.scaledDensity * fontSize
        setMemoFontPxSize(baseHeightPx)
    }

    fun Context.specialPointToPixel(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            this.resources.displayMetrics
        )
    }
}