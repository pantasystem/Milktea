package net.pantasystem.milktea.common_android_ui

import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.text.getSpans
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.github.penfeizhou.animation.apng.APNGDrawable
import jp.panta.misskeyandroidclient.mfm.Root
import net.pantasystem.milktea.common_android.TextType
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.common_android.ui.text.DrawableEmojiSpan
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
        this.movementMethod = LinkMovementMethod.getInstance()
        stopDrawableAnimations(this)
        when (textType) {
            is TextType.Mastodon -> {
                this.text = CustomEmojiDecorator().decorate(
                    textType.html.spanned,
                    textType.html.accountHost,
                    textType.html.parserResult,
                    this
                )
            }
            is TextType.Misskey -> {
                val node = textType.root ?: return
                this.text = MFMDecorator.decorate(this, node)
            }
        }

    }

    @BindingAdapter("sourceText", "emojis")
    @JvmStatic
    fun TextView.decorateWithLowPerformance(sourceText: String?, emojis: List<Emoji>?) {
        sourceText ?: return
        emojis ?: return
        val node = MFMParser.parse(sourceText, emojis)
            ?: return
        this.movementMethod = LinkMovementMethod.getInstance()
        this.text = MFMDecorator.decorate(this, node)
    }
}