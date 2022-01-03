package jp.panta.misskeyandroidclient.view.emojis

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionSelection
import jp.panta.misskeyandroidclient.util.svg.GlideApp
import jp.panta.misskeyandroidclient.view.reaction.ReactionChoicesAdapter

object EmojiHelper{

    @JvmStatic
    @BindingAdapter("customEmoji")
    fun ImageView.setEmojiImage(customEmoji: Emoji){
        if(customEmoji.type?.contains("svg") == true || customEmoji.url?.contains("svg") == true|| customEmoji.uri?.contains("svg") == true){

            GlideApp.with(context)
                .`as`(Bitmap::class.java)
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