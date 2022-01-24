package jp.panta.misskeyandroidclient.view.emojis

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.util.glide.GlideApp

object EmojiHelper{

    @JvmStatic
    @BindingAdapter("customEmoji")
    fun ImageView.setEmojiImage(customEmoji: Emoji){
        if(customEmoji.type?.contains("svg") == true || customEmoji.url?.contains("svg") == true|| customEmoji.uri?.contains("svg") == true){

            GlideApp.with(context)
                .load(customEmoji.url?: customEmoji.url)
                .centerCrop()
                .into(this)
        }else{
            Glide.with(this.context)
                .load(customEmoji.url?: customEmoji.uri)
                .centerCrop()
                .into(this)

        }
    }
}