package net.pantasystem.milktea.common_android_ui

import android.app.Activity
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
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
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.common_android.mfm.Root
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
import net.pantasystem.milktea.common_navigation.SearchNavType
import net.pantasystem.milktea.common_navigation.UserDetailNavigationArgs
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji

object DecorateTextHelper {


    @BindingAdapter("textNode")
    @JvmStatic
    fun TextView.decorate(node: Root?) {
        node ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        stopDrawableAnimations(this)
        val lazy = MFMDecorator.decorate(node, LazyDecorateSkipElementsHolder())
        this.text = MFMDecorator.decorate(this, lazy)
    }

    fun stopDrawableAnimations(textView: TextView) {
        val beforeText = textView.text
        if (beforeText is Spannable) {
            val drawableEmojiSpans = beforeText.getSpans<DrawableEmojiSpan>()
            drawableEmojiSpans.forEach {
                when (val imageDrawable = it.imageDrawable) {
                    is GifDrawable -> {
                        imageDrawable.stop()
                        imageDrawable.clearAnimationCallbacks()
                    }
                    is APNGDrawable -> {
                        imageDrawable.stop()
                        imageDrawable.clearAnimationCallbacks()
                    }
                    else -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            if (imageDrawable is AnimatedImageDrawable) {
                                imageDrawable.stop()
                                imageDrawable.clearAnimationCallbacks()
                            }
                        }
                    }
                }
                it.imageDrawable?.callback = null
                it.imageDrawable = null
                it.adapter = null
                // NOTE: 不要になった画像リソースを解放している
                // NOTE: MFMDecoratorの仕様上現状はEmojiSpanを使いまわさないのでここでリソース破棄をしてしまっても問題ない。
//                Glide.with(textView).clear(it.target)
            }
        }
    }

    @BindingAdapter("textTypeSource", "customEmojiScale")
    @JvmStatic
    fun TextView.decorate(textType: TextType?, customEmojiScale: Float?) {
        textType ?: return
        stopDrawableAnimations(this)

        val emojiScale = customEmojiScale ?: 1.0f
        when (textType) {
            is TextType.Mastodon -> {
                val decoratedText = CustomEmojiDecorator().decorate(
                    textType.html.spanned,
                    textType.html.accountHost,
                    textType.html.parserResult,
                    this,
                    emojiScale,
                )
                this.text = decoratedText
                this.movementMethod = ClickListenableLinkMovementMethod { url ->

                    // NOTE: クリックしたURLを探している
                    val urlSpans = decoratedText.getSpans(0, decoratedText.length, URLSpan::class.java)
                    var textHashTag: CharSequence? = null
                    for (urlSpan in urlSpans) {
                        val start = decoratedText.getSpanStart(urlSpan)
                        val end = decoratedText.getSpanEnd(urlSpan)
                        val spannedText = decoratedText.subSequence(start, end)
                        if (spannedText.isNotEmpty() && spannedText[0] == '#') {
                            if (urlSpan.url == url) {
                                textHashTag = spannedText
                            }
                        }
                    }
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
                            val intent = navigationEntryPoint.searchNavigation().newIntent(SearchNavType.ResultScreen(
                                searchWord = "#${tag.name}"
                            ))
                            context.startActivity(intent)
                            true
                        }
                        textHashTag != null -> {
                            val intent = navigationEntryPoint.searchNavigation().newIntent(SearchNavType.ResultScreen(
                                searchWord = textHashTag.toString()
                            ))
                            context.startActivity(intent)
                            true
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
                this.text = MFMDecorator.decorate(this, textType.lazyDecorateResult, emojiScale)
            }
        }

    }

    @BindingAdapter("sourceText", "emojis", "account", "host")
    @JvmStatic
    fun TextView.decorateWithLowPerformance(sourceText: String?, emojis: List<CustomEmoji>?, account: Account?, host: String?) {
        sourceText ?: return
        emojis ?: return
        account ?: return
        host ?: return
        val node = MFMParser.parse(sourceText, emojis, accountHost = account.getHost(), userHost = host)
            ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        val lazy = MFMDecorator.decorate(node, LazyDecorateSkipElementsHolder())
        this.text = MFMDecorator.decorate(this, lazy)
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