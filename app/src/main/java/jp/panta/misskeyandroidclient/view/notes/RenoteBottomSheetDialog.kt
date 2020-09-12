package jp.panta.misskeyandroidclient.view.notes

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.dialog_renote.view.*

class RenoteBottomSheetDialog : BottomSheetDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.dialog_renote, null)
        dialog.setContentView(view)
        val miApplication = context?.applicationContext as MiApplication
        val account = miApplication.getCurrentAccount().value
        //val requestSetting =

        val activity = requireActivity()
        val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(miApplication)).get(NotesViewModel::class.java)

        if(account != null){

            val target = notesViewModel.reNoteTarget.event
            if(target?.isRenotedByMe == true){
                view.unRenoteBase.visibility = View.VISIBLE
            }else{
                view.unRenoteBase.visibility = View.GONE
            }

            view.unRenote.setOnClickListener {
                target?.let{
                    notesViewModel.unRenote(target)
                    dismiss()
                }
            }

            view.renote.setOnClickListener{
                notesViewModel.postRenote()
                dismiss()
            }

            view.quote_renote.setOnClickListener {
                notesViewModel.putQuoteRenoteTarget()
                dismiss()
            }

        }
        return dialog
    }


}