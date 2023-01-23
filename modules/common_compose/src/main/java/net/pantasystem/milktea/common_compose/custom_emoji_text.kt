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
import net.pantasystem.milktea.model.emoji.CustomEmojiParsedResult
import net.pantasystem.milktea.model.emoji.CustomEmojiParser
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.emoji.EmojiResolvedType
import net.pantasystem.milktea.model.instance.HostWithVersion


@Composable
@Stable
fun CustomEmojiText(
    modifier: Modifier = Modifier,
    text: String,
    emojis: List<Emoji>,
    accountHost: String? = null,
    sourceHost: String? = null,
    parsedResult: CustomEmojiParsedResult? = null,
    fontSize: TextUnit = 14.sp,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {


    val result = remember(text, emojis, accountHost, sourceHost) {
        parsedResult ?: CustomEmojiParser.parse(
            sourceHost = sourceHost,
            emojis = emojis,
            text = text
        ).let { result ->
            result.copy(
                emojis = result.emojis.filter {
                    HostWithVersion.isOverV13(accountHost) || it.result is EmojiResolvedType.Resolved
                }
            )
        }
    }


    val annotatedText = buildAnnotatedString {
        var pos = 0

        for (r in result.emojis) {
            if (pos != r.start) {
                append(result.text.substring(pos, r.start))
            }
            appendInlineContent(r.result.tag, text.substring(r.start, r.end))
            pos = r.end
        }
        if (pos != text.length && text.isNotBlank()) {
            append(text.substring(pos, text.length))
        }

    }

    val inlineContents = result.emojis.associate { emojiPos ->
        emojiPos.result.tag to InlineTextContent(
            Placeholder(
                width = fontSize,
                height = fontSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
            )
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = emojiPos.result.getUrl(accountHost)),
                contentDescription = null
            )
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