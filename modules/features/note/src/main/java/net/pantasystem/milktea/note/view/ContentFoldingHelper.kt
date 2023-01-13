package net.pantasystem.milktea.note.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.note.viewmodel.CwTextGenerator
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
            val buttonText = CwTextGenerator(foldingNote.toShowNote, folding)

            foldingButton?.visibility = View.VISIBLE

            foldingButton?.text = buttonText.getString(context)
        }

        foldingContent?.visibility = if (folding) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

}