package net.pantasystem.milktea.common_android_ui

import android.app.Activity
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.github.penfeizhou.animation.apng.APNGDrawable
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.internal.managers.FragmentComponentManager
import jp.panta.misskeyandroidclient.mfm.Root
import net.pantasystem.milktea.common_android.TextType
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji

object DecorateTextHelper {


    @BindingAdapter("textNode")
    @JvmStatic
    fun TextView.decorate(node: Root?) {
        node ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        stopDrawableAnimations(this)
        this.text = MFMDecorator.decorate(this, node)
    }

    fun stopDrawableAnimations(textView: TextView) {
        val beforeText = textView.text
        if (beforeText is Spannable) {
            val drawableEmojiSpans = beforeText.getSpans<DrawableEmojiSpan>()
            drawableEmojiSpans.forEach {
                when (val imageDrawable = it.imageDrawable) {
                    is GifDrawable -> {
                        imageDrawable.stop()
                    }
                    is APNGDrawable -> {
                        imageDrawable.stop()
                    }
                }
            }
        }
    }

    @BindingAdapter("textTypeSource")
    @JvmStatic
    fun TextView.decorate(textType: TextType?) {
        textType ?: return
        stopDrawableAnimations(this)
        when (textType) {
            is TextType.Mastodon -> {
                this.text = CustomEmojiDecorator().decorate(
                    textType.html.spanned,
                    textType.html.accountHost,
                    textType.html.parserResult,
                    this
                )
                this.movementMethod = ClickListenableLinkMovementMethod { url ->
                    val tag = textType.tags.firstOrNull {
                        it.url == url || it.url == url.lowercase()
                    }
                    val mention = textType.mentions.firstOrNull {
                        it.url == url
                    }
                    Log.d("DecorateTextHelper", "clicked url:$url, tag:$tag, mention:$mention, tags:${textType.tags}")
                    val activity = FragmentComponentManager.findActivity(context) as Activity
                    val navigationEntryPoint = EntryPointAccessors.fromActivity(
                        activity,
                        NavigationEntryPointForBinding::class.java
                    )
                    when {
                        tag != null -> {
                            // FIXME: タグの場合うまく動作しないケースがある
                            // 原因としてTagオブジェクトに入っているURLとHTML上に表示されているURLが異なるから
                            false
                        }
                        mention != null -> {
                            val intent = navigationEntryPoint
                                .userDetailNavigation()
                                .newIntent(UserDetailNavigationArgs.UserName(userName = mention.acct))
                            context.startActivity(intent)
                            true
                        }
                        else -> false
                    }

                }
            }
            is TextType.Misskey -> {
                this.movementMethod = LinkMovementMethod.getInstance()
                val node = textType.root ?: return
                this.text = MFMDecorator.decorate(this, node)
            }
        }

    }

    @BindingAdapter("sourceText", "emojis", "account", "host")
    @JvmStatic
    fun TextView.decorateWithLowPerformance(sourceText: String?, emojis: List<Emoji>?, account: Account?, host: String?) {
        sourceText ?: return
        emojis ?: return
        account ?: return
        host ?: return
        val node = MFMParser.parse(sourceText, emojis, accountHost = account.getHost(), userHost = host)
            ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        this.text = MFMDecorator.decorate(this, node)
    }
}


class ClickListenableLinkMovementMethod(private val onClick: ((url: String) -> Boolean)) :
    LinkMovementMethod() {
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val url = getUrl(widget, buffer, event)
        return when {
            event.action == MotionEvent.ACTION_UP && url != null && onClick(url) -> true
            else -> super.onTouchEvent(widget, buffer, event)
        }
    }

    companion object {
        private fun getUrl(widget: TextView, buffer: Spannable, event: MotionEvent): String? {
            val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
            val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
            val off =
                widget.layout.run { getOffsetForHorizontal(getLineForVertical(y), x.toFloat()) }
            return (buffer.getSpans(off, off, ClickableSpan::class.java)
                .getOrNull(0) as? URLSpan)?.url
        }
    }
}