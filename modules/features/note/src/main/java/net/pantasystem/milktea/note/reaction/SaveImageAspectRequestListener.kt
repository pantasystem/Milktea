package net.pantasystem.milktea.note.reaction

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class SaveImageAspectRequestListener(
    val url: String?
) : RequestListener<Drawable> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean,
    ): Boolean {
        resource ?: return false
        val imageAspectRatio: Float = resource.intrinsicWidth.toFloat() / resource.intrinsicHeight
        ImageAspectRatioCache.put(url, imageAspectRatio)
        return false
    }
}