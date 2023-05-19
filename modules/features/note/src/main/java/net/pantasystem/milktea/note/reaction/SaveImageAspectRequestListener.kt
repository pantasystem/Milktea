package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.EntryPointAccessors
import net.pantasystem.milktea.common_android_ui.BindingProvider
import net.pantasystem.milktea.model.emoji.Emoji

class SaveImageAspectRequestListener(
    val emoji: Emoji,
    val context: Context,
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
        val navigationEntryPoint = EntryPointAccessors.fromApplication(
            context,
            BindingProvider::class.java
        )
        (emoji.url ?: emoji.uri)?.let {
            navigationEntryPoint.customEmojiAspectRatioStore().save(
                emoji, imageAspectRatio
            )

        }

        ImageAspectRatioCache.put(emoji.url ?: emoji.uri, imageAspectRatio)
        return false
    }
}