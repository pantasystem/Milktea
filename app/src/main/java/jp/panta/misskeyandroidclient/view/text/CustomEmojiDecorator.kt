package jp.panta.misskeyandroidclient.view.text

import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.util.svg.GlideApp
import jp.panta.misskeyandroidclient.util.svg.SvgSoftwareLayerSetter
import java.util.regex.Pattern
import  com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.load.resource.gif.GifDrawable

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

                /*
                GlideApp.with(this.context)
                        .`as`(PictureDrawable::class.java)
                        .listener(SvgSoftwareLayerSetter())
                        .load(emoji.url?: emoji.uri)
                        .centerCrop()
                        .transition(withCrossFade())
                        .into(reactionImageView)
                 */
                if(emoji.isSvg()){
                    GlideApp.with(view.context)
                        .`as`(GifDrawable::class.java)
                        //.listener(SvgSoftwareLayerSetter())
                        //.transition(withCrossFade())
                        .load(emoji.url?: emoji.url)
                        .into(span.target)


                }else{
                    Glide.with(view)
                        .asGif()
                        .load(emoji.url)
                        .into(span.target)
                }

            }
        }
        return builder
    }
}