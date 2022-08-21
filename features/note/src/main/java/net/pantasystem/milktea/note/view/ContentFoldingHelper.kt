package net.pantasystem.milktea.note.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData

object ContentFoldingHelper {



    @BindingAdapter("foldingNote", "cw", "foldingButton","foldingContent", "isFolding")
    @JvmStatic
    fun ViewGroup.setFoldingState(foldingNote: PlaneNoteViewData?, cw: TextView?, foldingButton: TextView?, foldingContent: ViewGroup?, isFolding: Boolean?){

        val isVisible = foldingNote?.cw != null && foldingNote.cw.isNotBlank()
        cw?.isVisible = isVisible


        val folding = isFolding ?: false


        if (foldingNote?.cw == null) {
            foldingButton?.visibility = View.GONE
        } else {
            foldingButton?.visibility = View.VISIBLE
            if (folding) {
                foldingButton?.text = context.getString(R.string.show_more, foldingNote.text?.codePointCount(0, foldingNote.text.length))
            } else {
                foldingButton?.text = context.getString(R.string.hide)
            }
        }

        foldingContent?.visibility = if (folding) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

}