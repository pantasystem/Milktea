package net.pantasystem.milktea.note.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.mfm.MFMParser
import net.pantasystem.milktea.common_android_ui.LazyDecorateSkipElementsHolder
import net.pantasystem.milktea.common_android_ui.MFMDecorator
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Translation
import net.pantasystem.milktea.note.R

object TranslationHelper {


    @JvmStatic
    @BindingAdapter("translationState", "emojis")
    fun TextView.setTranslatedText(state: ResultState<Translation>?, emojis: List<CustomEmoji>?) {
        if(state == null) {
            this.visibility = View.GONE
            return
        }

        if(state is ResultState.Loading) {
            this.visibility = View.GONE
            return
        }

        val translation = runCancellableCatching {
            (state.content as StateContent.Exist).rawContent
        }.getOrNull()
        this.visibility = View.VISIBLE

        if(state is ResultState.Error) {
            this.text = context.getString(R.string.error_s, state.throwable.toString())
        }
        if(translation == null) {
            return
        }

        val text = context.getString(R.string.translated_from_s, translation.sourceLang) + translation.text
        val root = MFMParser.parse(text, emojis)!!
        val lazy = MFMDecorator.decorate(root, LazyDecorateSkipElementsHolder())
        this.text = MFMDecorator.decorate(this, lazy)

    }

    @JvmStatic
    @BindingAdapter("translationState")
    fun ViewGroup.translationVisibility(state: ResultState<Translation>?) {
        if(state == null) {
            this.visibility = View.GONE
            return
        }
        this.visibility = if(state.content is StateContent.NotExist && state is ResultState.Fixed) {
            View.GONE
        }else{
            View.VISIBLE
        }
    }
}