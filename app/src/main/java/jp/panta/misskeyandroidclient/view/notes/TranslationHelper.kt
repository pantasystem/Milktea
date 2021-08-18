package jp.panta.misskeyandroidclient.view.notes

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.mfm.MFMDecorator
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.Translation
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent

object TranslationHelper {


    @JvmStatic
    @BindingAdapter("translationState", "emojis")
    fun TextView.setTranslatedText(state: State<Translation>?, emojis: List<Emoji>) {
        if(state == null) {
            this.visibility = View.GONE
            return
        }

        if(state is State.Loading) {
            this.visibility = View.GONE
            return
        }
        if(state.content is StateContent.NotExist) {
            this.visibility = View.GONE
            return
        }
        val translation = runCatching {
            (state.content as StateContent.Exist).rawContent
        }.getOrNull()
        if(translation == null) {
            this.visibility = View.GONE
            return
        }
        this.visibility = View.VISIBLE

        val text = context.getString(R.string.translated_from_s, translation.sourceLang) + translation.text
        val root = MFMParser.parse(text, emojis)!!

        MFMDecorator.decorate(this, root)

    }

    @JvmStatic
    @BindingAdapter("translationState")
    fun ViewGroup.translationVisibility(state: State<Translation>?) {
        if(state == null) {
            this.visibility = View.GONE
            return
        }
        this.visibility = if(state.content is StateContent.NotExist && state !is State.Loading) {
            View.GONE
        }else{
            View.VISIBLE
        }
    }
}