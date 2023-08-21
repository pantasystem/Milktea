package net.pantasystem.milktea.common.glide

import android.app.Activity

import android.content.Context




object GlideUtils {
    fun isAvailableContextForGlide(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        if (context is Activity) {
            if (context.isDestroyed || context.isFinishing) {
                return false
            }
        }
        return true
    }
}