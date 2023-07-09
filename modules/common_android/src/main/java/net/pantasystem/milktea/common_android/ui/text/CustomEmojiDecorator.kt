package net.pantasystem.milktea.common_android.ui.text

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.widget.TextView
import net.pantasystem.milktea.common.glide.GlideApp
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.EmojiResolvedType
import kotlin.math.max

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
            it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(
                emojiAdapter,
                it.result.getUrl(accountHost),
                (it.result as? EmojiResolvedType.Resolved)?.emoji?.aspectRatio
            )
            GlideApp.with(view)
                .asDrawable()
                .load(it.result.getUrl(accountHost))
                .override(view.textSize.toInt())
                .into(span.target)
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

    fun decorate(accountHost: String?, result: CustomEmojiParsedResult, view: TextView): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(result.text)

        result.emojis.filter {
            it.result is EmojiResolvedType.Resolved
        }.map {
            val span = DrawableEmojiSpan(
                emojiAdapter,
                it.result.getUrl(accountHost),
                (it.result as? EmojiResolvedType.Resolved)?.emoji?.aspectRatio
            )
            GlideApp.with(view)
                .asDrawable()
                .override(view.textSize.toInt())
                .load(it.result.getUrl(accountHost))
                .into(span.target)
            builder.setSpan(span, it.start, it.end, 0)
        }


        return builder
    }

    fun decorate(
        spanned: Spanned,
        accountHost: String?,
        result: CustomEmojiParsedResult,
        view: TextView,
        customEmojiScale: Float = 1f,
    ): Spanned {

        val emojiAdapter = EmojiAdapter(view)
        val builder = SpannableStringBuilder(spanned)

        result.emojis.filter {
            it.result is EmojiResolvedType.Resolved
        }.map {
            val aspectRatio = (it.result as? EmojiResolvedType.Resolved)?.emoji?.aspectRatio
            val span = DrawableEmojiSpan(
                emojiAdapter,
                it.result.getUrl(accountHost),
                aspectRatio,
            )
            val height = max(view.textSize * 0.75f, 10f)
            val width = when(aspectRatio) {
                null -> height
                else -> height * aspectRatio
            }

            GlideApp.with(view)
                .asDrawable()
                .load(it.result.getUrl(accountHost))
                .override((width * customEmojiScale).toInt(), (height * customEmojiScale).toInt())
                .into(span.target)
            builder.setSpan(span, it.start, it.end, 0)
            builder.setSpan(RelativeSizeSpan(customEmojiScale), it.start, it.end, 0)
        }


        return builder
    }

}

