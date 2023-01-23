package net.pantasystem.milktea.common_android_ui

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter

object DrawableTintCompat {

    @JvmStatic
    @BindingAdapter("drawableTintCompat")
    fun TextView.setDrawableTintCompat(c: Int?){

        val color: Int = c?: this.currentTextColor

        val drawables = this.compoundDrawables
        drawables.forEachIndexed { i, d ->
            val drawable: Drawable? = DrawableCompat.wrap(d)
            if(drawable != null){
                drawable.setTint(color)
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN)
                drawables[i] = drawable
            }

        }
        this.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])

    }
}