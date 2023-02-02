package net.pantasystem.milktea.common_android.resource

import android.content.Context
import android.util.DisplayMetrics

fun Context.convertDp2Px(dp: Float): Float {
    val metrics: DisplayMetrics = resources.displayMetrics
    return dp * metrics.density
}