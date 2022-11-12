package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.model.emoji.Emoji
import java.util.regex.Pattern

data class EmojiPos(
    val emoji: Emoji,
    val start: Int,
    val end: Int
)

fun String.findCustomEmojiInText(emojis: List<Emoji>): List<EmojiPos> {

    val pattern = StringBuilder(":(").also { patternBuilder ->
        emojis.forEachIndexed { index, emoji ->
            patternBuilder.append(Pattern.quote(emoji.name))
            if (emojis.size - 1 != index) {
                patternBuilder.append("|")
            }
        }
        patternBuilder.append("):")
    }.toString()

    val matcher = Pattern.compile(pattern).matcher(this)

    val matches = mutableListOf<EmojiPos>()

    while (matcher.find()) {
        val emoji = emojis.firstOrNull {
            it.name == this.substring(matcher.start() + 1, matcher.end() - 1)
        }
        if (emoji != null) {
            matches.add(
                EmojiPos(
                    emoji,
                    matcher.start(),
                    matcher.end()
                )
            )
        }
    }
    return matches
}


@Composable
@Stable
fun CustomEmojiText(
    modifier: Modifier = Modifier,
    text: String,
    emojis: List<Emoji>,
    fontSize: TextUnit = 14.sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {

    val matches = remember(text, emojis) {
        text.findCustomEmojiInText(emojis)
    }

    val annotatedText = buildAnnotatedString {
        var pos = 0

        for (m in matches) {
            if (pos != m.start) {
                append(text.substring(pos, m.start))
            }
            appendInlineContent(m.emoji.name, text.substring(m.start, m.end))
            pos = m.end
        }
        if (pos != text.length && text.isNotBlank()) {
            append(text.substring(pos, text.length))
        }
    }

    val inlineContents = emojis.associate { emoji ->
        emoji.name to InlineTextContent(
            Placeholder(
                width = fontSize,
                height = fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
            )
        ) {
            Image(painter = rememberAsyncImagePainter(model = emoji.url), contentDescription = null)
        }
    }
    Text(
        annotatedText,
        inlineContent = inlineContents,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        maxLines = maxLines,
        modifier = modifier,
        textAlign = textAlign,
        overflow = overflow
    )
}