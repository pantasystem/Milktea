package jp.panta.misskeyandroidclient.view.text

import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.model.emoji.Emoji

object DecoratTextHelper {
    @BindingAdapter("text", "emojis")
    @JvmStatic
    fun TextView.decorate(text: String?, emojis: List<Emoji>?){
        text?: return
        val span = CustomEmojiDecorator()
            .decorate(emojis, text, this)
        this.text = span
    }
}