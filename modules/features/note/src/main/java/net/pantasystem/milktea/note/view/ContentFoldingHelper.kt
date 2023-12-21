package net.pantasystem.milktea.note.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import net.pantasystem.milktea.common_android.resource.getString
import net.pantasystem.milktea.common_android.ui.VisibilityHelper.setMemoVisibility
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
            foldingButton?.setMemoVisibility(View.GONE)
        } else {
            val buttonText = CwTextGenerator(foldingNote.toShowNote, folding)

            foldingButton?.setMemoVisibility(View.VISIBLE)

            foldingButton?.text = buttonText.getString(context)
        }

        foldingContent?.setMemoVisibility(if (folding) {
            View.GONE
        } else {
            View.VISIBLE
        })
    }

}