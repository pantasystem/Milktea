package net.pantasystem.milktea.note.view

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R

object MastodonFavoriteButtonHelper {

    @JvmStatic
    @BindingAdapter("favoriteButtonIcon")
    fun ImageButton.setFavoriteButtonState(note: Note?) {
        note ?: return
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.normalIconTint, typedValue, true)
        val normalTintColor = typedValue.data

        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        when(val type = note.type) {
            is Note.Type.Mastodon -> {
                if (type.favorited == true) {
                    this.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp))
                    this.imageTintList = ColorStateList.valueOf(primaryColor)
                } else {
                    this.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_border_black_24dp))
                    this.imageTintList = ColorStateList.valueOf(normalTintColor)
                }
            }
            Note.Type.Misskey -> {
                this.visibility = View.GONE
            }
        }
    }
}