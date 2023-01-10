package net.pantasystem.milktea.common_android.ui.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.EmojiResolvedType
import net.pantasystem.milktea.model.instance.HostWithVersion

class CustomEmojiDecorator {

    fun decorate(
        accountHost: String?,
        sourceHost: String?,
        emojis: List<Emoji>?,
        text: String,
        view: View,
    ): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(text)


        val result = CustomEmojiParser.parse(
            sourceHost,
            emojis,
            text,
        )
        result.emojis.filter {
            HostWithVersion.isOverV13(accountHost)  || it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(emojiAdapter)
            GlideApp.with(view)
                .asDrawable()
                .load(it.result.getUrl(accountHost))
                .into(span.target)
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

    fun decorate(accountHost: String?, result: CustomEmojiParsedResult, view: View): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(result.text)

        result.emojis.filter {
            HostWithVersion.isOverV13(accountHost) || it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(emojiAdapter)
            GlideApp.with(view)
                .asDrawable()
                .load(it.result.getUrl(accountHost))
                .into(span.target)
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }
}

