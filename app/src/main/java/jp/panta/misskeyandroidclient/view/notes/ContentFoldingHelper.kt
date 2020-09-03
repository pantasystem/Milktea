package jp.panta.misskeyandroidclient.view.notes

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData

object ContentFoldingHelper {


    @BindingAdapter("foldingNote", "cw", "foldingButton","foldingContent", "isFolding")
    @JvmStatic
    fun ViewGroup.setFoldingState(foldingNote: PlaneNoteViewData?, cw: TextView?, foldingButton: TextView?, foldingContent: ViewGroup?, isFolding: Boolean?){
        foldingNote?:return

        val isFoldContent = isFolding?: false

        val isNeedFold = foldingNote.cw?.isNotBlank() == true || foldingNote.determineTextLength.isLong()
        if(foldingNote.cw.isNullOrBlank()){
            if(isNeedFold){
                cw?.visibility = View.VISIBLE
                cw?.text = String.format(context.getString(R.string.long_text, foldingNote.text?.codePointCount(0, foldingNote.text.length)?:0))
            }else{
                cw?.visibility = View.GONE
            }
        }else{
            cw?.visibility = View.VISIBLE

        }

        if(isNeedFold){
            foldingButton?.visibility = View.VISIBLE
            foldingButton?.text = String.format(context.getString(R.string.show_more), foldingNote.text?.codePointCount(0, foldingNote.text.length)?:0 )

            if(isFoldContent){
                foldingContent?.visibility = View.GONE
            }else{
                foldingContent?.visibility = View.VISIBLE
            }
        }else{
            foldingButton?.visibility = View.GONE
            foldingContent?.visibility = View.VISIBLE
        }

    }

}