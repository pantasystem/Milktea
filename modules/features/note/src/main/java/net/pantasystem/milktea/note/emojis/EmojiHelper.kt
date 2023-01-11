package net.pantasystem.milktea.note.emojis

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.emoji.Emoji

object EmojiHelper{

    @JvmStatic
    @BindingAdapter("customEmoji")
    fun ImageView.setEmojiImage(customEmoji: Emoji){
        GlideApp.with(this.context)
            .load(customEmoji.url?: customEmoji.uri)
                // FIXME: webpの場合うまく表示できなくなる
//            .centerCrop()
            .into(this)
    }
}