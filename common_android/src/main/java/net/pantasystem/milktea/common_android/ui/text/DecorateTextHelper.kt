package net.pantasystem.milktea.common_android.ui.text

import android.net.Uri
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common_android.mfm.MFMDecorator
import jp.panta.misskeyandroidclient.mfm.Root
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.model.emoji.Emoji
import java.util.regex.Pattern

object DecorateTextHelper {
    private const val HASH_TAG_PETTERN = """#([^\s.,!?'"#:/\[\]【】@]+)"""
    private const val MENSTION_PETTERN = """@\w([\w-]*\w)?(?:@[\w.\-]+\w)?"""
    private const val WEB_URL_PATTERN = """(https?|ftp)(://[-_.!~*'()a-zA-Z0-9;/?:@&=+$,%#]+)"""

    private const val SCHEME = "misskey:"

    private val hashTagPattern = Pattern.compile(HASH_TAG_PETTERN)
    private val mentionPattern = Pattern.compile(MENSTION_PETTERN, Pattern.MULTILINE)
    private val webUrlPattern = Pattern.compile(WEB_URL_PATTERN)

    @BindingAdapter("text", "emojis", "clickableLink")
    @JvmStatic
    fun TextView.decorate(text: String?, emojis: List<Emoji>?, clickableLink: Boolean? = null){
        text?: return
        val span = CustomEmojiDecorator()
            .decorate(emojis, text, this)
        this.text = span
        if(clickableLink == true){
            decorateLink(this)
        }
    }

    private fun decorateLink(textView: TextView){
        Linkify.addLinks(textView, mentionPattern, SCHEME, null) { _, url ->
            val builder = Uri.Builder()
            builder.authority("user")
            builder.appendQueryParameter("userName", url)
            builder.build().toString()
        }

        Linkify.addLinks(textView, hashTagPattern, SCHEME, null) { _, url ->
            val builder = Uri.Builder()
                .authority("search")
                .appendQueryParameter("keyword", url)
            builder.path(url).toString()
        }

        Linkify.addLinks(textView, webUrlPattern, null, null) { _, url ->
            url
        }
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    @BindingAdapter("textNode")
    @JvmStatic
    fun TextView.decorate(node: Root?){
        node?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        this.text = MFMDecorator.decorate(this, node)
    }

    @BindingAdapter("sourceText", "emojis")
    @JvmStatic
    fun TextView.decorateWithLowPerformance(sourceText: String?, emojis: List<Emoji>?){
        sourceText?: return
        emojis?: return
        val node = MFMParser.parse(sourceText, emojis)
            ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        this.text = MFMDecorator.decorate(this, node)
    }
}