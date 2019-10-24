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
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.TimelineViewModelFactory

class RenoteBottomSheetDialog : BottomSheetDialogFragment(){

    companion object{
        fun newInstance(setting: NoteRequest.Setting){

        }
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.dialog_renote, null)
        dialog.setContentView(view)

        val miApplication = context?.applicationContext as MiApplication
        val cn = miApplication.currentConnectionInstanceLiveData.value
         //val requestSetting =
        val tc = miApplication.timelineCapture
        val nc = miApplication.noteCapture

        val store = parentFragment?.viewModelStore
        if(store == null){
            Log.d("RenoteBottomSheetDialog", "store is null")
        }
        if(cn != null && store != null){
            //val viewModel = ViewModelProvider(store, factory).get(, NotesViewModel::class.java)
            //val factory = TimelineViewModelFactory(cn, NoteRequest.Setting("", NoteType.HOME), nc, tc)

            //val viewModel = ViewModelProvider(store, factory).get(TimelineViewModel::class.java)
            //Log.d("RenoteBottomSheetDialog", "対象のノート: ${viewModel.reNoteTarget.value?.toShowNote}")
        }
        parentFragment?.viewModelStore
        //val viewModel =
    }
}