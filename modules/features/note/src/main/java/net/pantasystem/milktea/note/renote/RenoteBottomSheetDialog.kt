package net.pantasystem.milktea.note.renote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RenoteBottomSheetDialog : BottomSheetDialogFragment() {

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

    val viewModel by activityViewModels<RenoteViewModel>()


    val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
    }

    val isRenotedByMe by lazy {
        requireArguments().getBoolean("IS_RENOTED_BY_ME", false)
    }

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//
//        val dialog = super.onCreateDialog(savedInstanceState)
//        val view = View.inflate(context, R.layout.dialog_renote, null)
//        dialog.setContentView(view)
//        val binding = DialogRenoteBinding.bind(view)
//        val account = accountStore.currentAccount
//        //val requestSetting =
//
//
//        if(account != null){
//
//            if(isRenotedByMe){
//                binding.unRenoteBase.visibility = View.VISIBLE
//
//            }else{
//                binding.unRenoteBase.visibility = View.GONE
//            }
//
//            binding.unRenote.setOnClickListener {
//                notesViewModel.unRenote(noteId)
//                dismiss()
//            }
//
//            binding.renote.setOnClickListener{
//                notesViewModel.renote(noteId)
//                dismiss()
//            }
//
//            binding.quoteRenote.setOnClickListener {
//                notesViewModel.showQuoteNoteEditor(noteId)
//                dismiss()
//            }
//
//        }
//        return dialog
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setTargetNoteId(noteId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MdcTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RenoteTargetAccountRowList(accounts = uiState.accounts, onClick = {
                                viewModel.toggleAddAccount(it)
                            })
                            RenoteButtonLayout(
                                onClick = {
                                    viewModel.renote()
                                    dismiss()
                                },
                                icon = Icons.Default.Repeat,
                                text = stringResource(id = R.string.renote)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            if (isRenotedByMe) {
                                RenoteButtonLayout(
                                    onClick = {
                                        notesViewModel.unRenote(noteId)
                                        dismiss()
                                    },
                                    icon = Icons.Default.FormatQuote,
                                    text = stringResource(id = R.string.unrenote)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                            }

                            RenoteButtonLayout(
                                onClick = {
                                    notesViewModel.showQuoteNoteEditor(noteId)
                                    dismiss()
                                },
                                icon = Icons.Default.FormatQuote,
                                text = stringResource(id = R.string.quote_renote)
                            )
                        }
                    }
                }
            }
        }
    }
}

