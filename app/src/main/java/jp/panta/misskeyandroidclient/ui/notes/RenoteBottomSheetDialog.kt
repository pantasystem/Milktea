package jp.panta.misskeyandroidclient.ui.notes

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogRenoteBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory

class RenoteBottomSheetDialog : BottomSheetDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.dialog_renote, null)
        dialog.setContentView(view)
        val binding = DialogRenoteBinding.bind(view)
        val miApplication = context?.applicationContext as MiApplication
        val account = miApplication.getCurrentAccount().value
        //val requestSetting =

        val activity = requireActivity()
        val notesViewModel = ViewModelProvider(activity, NotesViewModelFactory(miApplication)).get(NotesViewModel::class.java)

        if(account != null){

            val target = notesViewModel.reNoteTarget.event
            if(target?.isRenotedByMe == true){
                binding.unRenoteBase.visibility = View.VISIBLE

            }else{
                binding.unRenoteBase.visibility = View.GONE
            }

            binding.unRenote.setOnClickListener {
                target?.let{
                    notesViewModel.unRenote(target)
                    dismiss()
                }
            }

            binding.renote.setOnClickListener{
                notesViewModel.postRenote()
                dismiss()
            }

            binding.quoteRenote.setOnClickListener {
                notesViewModel.putQuoteRenoteTarget()
                dismiss()
            }

        }
        return dialog
    }


}