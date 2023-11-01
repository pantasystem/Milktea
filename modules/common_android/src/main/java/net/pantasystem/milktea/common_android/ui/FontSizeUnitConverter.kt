package net.pantasystem.milktea.common_android.ui

import android.content.Context
import android.util.TypedValue
import android.widget.TextView

object FontSizeUnitConverter {
    fun TextView.setMemoFontPxSize(fontSize: Float) {
        if (this.textSize == fontSize) {
            return
        }
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
    }

    fun TextView.setMemoFontSpSize(fontSize: Float) {
        val baseHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            fontSize,
            this.resources.displayMetrics
        )
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