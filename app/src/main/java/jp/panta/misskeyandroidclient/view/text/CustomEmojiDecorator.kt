package jp.panta.misskeyandroidclient.view.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.util.glide.GlideApp
import java.util.regex.Pattern

class CustomEmojiDecorator{

    fun decorate(emojis: List<Emoji>?, text: String, view: View): Spanned {
        if(emojis.isNullOrEmpty()){
            return SpannableStringBuilder(text)
        }
        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(text)
        for(emoji in emojis){
            val pattern = ":${emoji.name}:"
            val matcher = Pattern.compile(pattern).matcher(text)
            while(matcher.find()){
                val span: EmojiSpan<*>

                if(emoji.isSvg()){
                    span = DrawableEmojiSpan(emojiAdapter)

                    GlideApp.with(view.context)
                        //.listener(SvgSoftwareLayerSetter())
                        //.transition(withCrossFade())
                        .load(emoji.url?: emoji.url)
                        .into(span.target)


                }else{
                    span = DrawableEmojiSpan(emojiAdapter)
                    Glide.with(view)
                        .asDrawable()
                        .load(emoji.url)
                        .into(span.target)
                }

                builder.setSpan(span, matcher.start(), matcher.end(), 0)


            }
        }
        emojiAdapter.subscribe()
        return builder
    }
}