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
    const val HASH_TAG_PETTERN = """#([^\s.,!?'"#:\/\[\]【】@]+)"""
    const val MENSTION_PETTERN = """@\w([\w-]*\w)?(?:@[\w.\-]+\w)?"""
    const val WEB_URL_PATTERN = """http(s)?://([\w-]+\.)+[\w-]+(/[\w- ./?%&=]*)?"""

    const val SCHEME = "misskey:"

    val hashTagPattern = Pattern.compile(HASH_TAG_PETTERN)
    val mentionPattern = Pattern.compile(MENSTION_PETTERN)
    val webUrlPattern = Pattern.compile(WEB_URL_PATTERN)

    @BindingAdapter("text", "emojis")
    @JvmStatic
    fun TextView.decorate(text: String?, emojis: List<Emoji>?){
        text?: return
        val span = CustomEmojiDecorator()
            .decorate(emojis, text, this)
        this.text = span
        decorateLink(this)
    }

    private fun decorateLink(textView: TextView){
        Linkify.addLinks(textView, mentionPattern, SCHEME, null, Linkify.TransformFilter { match, url ->
            val builder = Uri.Builder()
            builder.authority("user")
            builder.appendQueryParameter("userName", url)
            builder.build().toString()
        })

        Linkify.addLinks(textView, hashTagPattern, SCHEME, null, Linkify.TransformFilter { match, url ->
            val builder = Uri.Builder()
                .authority("search")
            builder.path(url).toString()
        })

        Linkify.addLinks(textView, webUrlPattern, null, null, Linkify.TransformFilter { _, url ->
            url
        })
        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}