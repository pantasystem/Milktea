package jp.panta.misskeyandroidclient.view.text

import android.net.Uri
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Patterns
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import java.util.regex.Pattern

object DecorateTextHelper {
    const val HASH_TAG_PETTERN = """#([^\s.,!?'"#:/\[\]【】@]+)"""
    const val MENSTION_PETTERN = """@\w([\w-]*\w)?(?:@[\w.\-]+\w)?"""
    const val WEB_URL_PATTERN = """(https?|ftp)(://[-_.!~*'()a-zA-Z0-9;/?:@&=+$,%#]+)"""

    const val SCHEME = "misskey:"

    val hashTagPattern = Pattern.compile(HASH_TAG_PETTERN)
    val mentionPattern = Pattern.compile(MENSTION_PETTERN, Pattern.MULTILINE)
    val webUrlPattern = Pattern.compile(WEB_URL_PATTERN)

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
        Linkify.addLinks(textView, mentionPattern, SCHEME, null, Linkify.TransformFilter { _, url ->
            val builder = Uri.Builder()
            builder.authority("user")
            builder.appendQueryParameter("userName", url)
            builder.build().toString()
        })

        Linkify.addLinks(textView, hashTagPattern, SCHEME, null, Linkify.TransformFilter { _, url ->
            val builder = Uri.Builder()
                .authority("search")
                .appendQueryParameter("keyword", url)
            builder.path(url).toString()
        })

        Linkify.addLinks(textView, webUrlPattern, null, null, Linkify.TransformFilter { _, url ->
            url
        })
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}