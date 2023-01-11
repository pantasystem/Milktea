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
                // FIXME: リダイレクトが発生する場合にcenterCropを使用すると不具合が発生する
                // https://github.com/bumptech/glide/issues/4652
//            .centerCrop()
            .into(this)
    }
}