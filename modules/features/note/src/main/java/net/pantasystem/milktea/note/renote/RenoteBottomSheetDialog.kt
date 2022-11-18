package net.pantasystem.milktea.note.renote

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogRenoteBinding
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RenoteBottomSheetDialog : BottomSheetDialogFragment(){

    companion object {
        fun newInstance(noteId: Note.Id, isRenotedByMe: Boolean): RenoteBottomSheetDialog {
            return RenoteBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putLong("ACCOUNT_ID", noteId.accountId)
                    putString("NOTE_ID", noteId.noteId)
                    putBoolean("IS_RENOTED_BY_ME", isRenotedByMe)
                }
            }
        }
    }

    val notesViewModel by activityViewModels<NotesViewModel>()

    @Inject
    lateinit var accountStore: AccountStore


    val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
    }

    val isRenotedByMe by lazy {
        requireArguments().getBoolean("IS_RENOTED_BY_ME", false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        val view = View.inflate(context, R.layout.dialog_renote, null)
        dialog.setContentView(view)
        val binding = DialogRenoteBinding.bind(view)
        val account = accountStore.currentAccount
        //val requestSetting =


        if(account != null){

            if(isRenotedByMe){
                binding.unRenoteBase.visibility = View.VISIBLE

            }else{
                binding.unRenoteBase.visibility = View.GONE
            }

            binding.unRenote.setOnClickListener {
                notesViewModel.unRenote(noteId)
                dismiss()
            }

            binding.renote.setOnClickListener{
                notesViewModel.renote(noteId)
                dismiss()
            }

            binding.quoteRenote.setOnClickListener {
                notesViewModel.showQuoteNoteEditor(noteId)
                dismiss()
            }

        }
        return dialog
    }


}