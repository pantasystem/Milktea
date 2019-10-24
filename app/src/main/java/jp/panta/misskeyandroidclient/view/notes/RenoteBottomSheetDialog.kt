package jp.panta.misskeyandroidclient.view.notes

import android.app.Dialog
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModelFactory
import kotlinx.android.synthetic.main.dialog_renote.view.*

class RenoteBottomSheetDialog : BottomSheetDialogFragment(){


    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.dialog_renote, null)
        dialog.setContentView(view)

        val miApplication = context?.applicationContext as MiApplication
        val cn = miApplication.currentConnectionInstanceLiveData.value
         //val requestSetting =
        val tc = miApplication.timelineCapture
        val nc = miApplication.noteCapture

        val activity = activity
        if(activity != null && cn != null){
            val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(cn, miApplication)).get(NotesViewModel::class.java)

            view.renote.setOnClickListener{
                notesViewModel.postRenote()
                dismiss()
            }

            view.quote_renote.setOnClickListener {
                notesViewModel.putQuoteRenoteTarget()
                dismiss()
            }

        }
        //val viewModel =
    }
}