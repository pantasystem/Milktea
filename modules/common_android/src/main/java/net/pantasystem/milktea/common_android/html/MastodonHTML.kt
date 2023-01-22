package net.pantasystem.milktea.common_android.html

import android.text.Spanned
import androidx.core.text.parseAsHtml
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.Emoji

data class MastodonHTML(
    val spanned: Spanned,
    val parserResult: CustomEmojiParsedResult,
    val accountHost: String?,
)

object MastodonHTMLParser {
    fun parse(
        text: String,
        emojis: List<Emoji>? = emptyList(),
        userHost: String? = null,
        accountHost: String? = null
    ): MastodonHTML {
        val spanned = text.replace("<br> ", "<br>&nbsp;")
            .replace("<br /> ", "<br />&nbsp;")
            .replace("<br/> ", "<br/>&nbsp;")
            .replace("  ", "&nbsp;&nbsp;").parseAsHtml().trim() as Spanned
        val result = CustomEmojiParser.parse(sourceHost = userHost, text = spanned.toString(), emojis = emojis)
        return MastodonHTML(
            spanned = spanned,
            result,
            accountHost = accountHost,
        )
    }
}