package net.pantasystem.milktea.common_android.ui.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.emoji.Emoji
import java.util.regex.Pattern

class CustomEmojiDecorator {

    fun decorate(emojis: List<Emoji>?, text: String, view: View): Spanned {
        if (emojis.isNullOrEmpty()) {
            return SpannableStringBuilder(text)
        }
        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(text)
        for (emoji in emojis) {
            val pattern = ":${emoji.name}:"
            val matcher = Pattern.compile(pattern).matcher(text)
            while (matcher.find()) {
                val span: EmojiSpan<*>

                span = DrawableEmojiSpan(emojiAdapter)
                GlideApp.with(view)
                    .asDrawable()
                    .load(emoji.url)
                    .into(span.target)
                builder.setSpan(span, matcher.start(), matcher.end(), 0)
            }
        }
        return builder
    }
}