package jp.panta.misskeyandroidclient.ui.notes.view

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

object ContentFoldingHelper {



    @BindingAdapter("foldingNote", "cw", "foldingButton","foldingContent", "isFolding")
    @JvmStatic
    fun ViewGroup.setFoldingState(foldingNote: PlaneNoteViewData?, cw: TextView?, foldingButton: TextView?, foldingContent: ViewGroup?, isFolding: Boolean?){

        cw?.visibility = if (foldingNote?.cw == null || foldingNote.cw.isBlank()) {
            View.GONE
        } else {
            View.VISIBLE
        }



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