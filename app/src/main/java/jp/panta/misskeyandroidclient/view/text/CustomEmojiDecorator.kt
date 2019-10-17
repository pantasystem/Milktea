package jp.panta.misskeyandroidclient.view.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import java.util.regex.Pattern

class CustomEmojiDecorator{

    fun decorate(emojis: List<Emoji>?, text: String, view: View): Spanned {
        if(emojis.isNullOrEmpty()){
            return SpannableStringBuilder(text)
        }
        val builder = SpannableStringBuilder(text)
        for(emoji in emojis){
            val pattern = ":${emoji.name}:"
            val matcher = Pattern.compile(pattern).matcher(text)
            while(matcher.find()){
                val span = EmojiSpan(view)
                builder.setSpan(span, matcher.start(), matcher.end(), 0)
                Glide.with(view)
                    .asBitmap()
                    .load(emoji.url)
                    .into(span.target)
            }
        }
        return builder
    }
}