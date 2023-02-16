package net.pantasystem.milktea.common_android.ui.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.EmojiResolvedType
import net.pantasystem.milktea.model.instance.HostWithVersion
import kotlin.math.min

class CustomEmojiDecorator {

    fun decorate(
        accountHost: String?,
        sourceHost: String?,
        emojis: List<Emoji>?,
        text: String,
        view: TextView,
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
                .override(min(view.textSize.toInt(), 640))
                .into(DrawableEmojiTarget(span))
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

    fun decorate(accountHost: String?, result: CustomEmojiParsedResult, view: TextView): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(result.text)

        result.emojis.filter {
            HostWithVersion.isOverV13(accountHost) || it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(emojiAdapter)
            GlideApp.with(view)
                .asDrawable()
                .override(min(view.textSize.toInt(), 640))
                .load(it.result.getUrl(accountHost))
                .into(DrawableEmojiTarget(span))
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

    fun decorate(spanned: Spanned, accountHost: String?, result: CustomEmojiParsedResult, view: TextView): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(spanned)

        result.emojis.filter {
            HostWithVersion.isOverV13(accountHost) || it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(emojiAdapter)
            GlideApp.with(view)
                .asDrawable()
                .load(it.result.getUrl(accountHost))
                .override(min(view.textSize.toInt(), 640))
                .into(DrawableEmojiTarget(span))
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

}

