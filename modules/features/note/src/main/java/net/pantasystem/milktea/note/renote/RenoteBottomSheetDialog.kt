package net.pantasystem.milktea.note.renote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class RenoteBottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "RenoteBottomSheetDialog"
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

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    val viewModel by activityViewModels<RenoteViewModel>()


    val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
    }

    private val isRenotedByMe by lazy {
        requireArguments().getBoolean("IS_RENOTED_BY_ME", false)
    }

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
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val uiState by viewModel.uiState.collectAsState()

                    RenoteDialogContent(
                        uiState = uiState,
                        isRenotedByMe = isRenotedByMe,
                        onToggleAddAccount = {
                            viewModel.toggleAddAccount(it)
                        },
                        onRenoteButtonClicked = {
                            viewModel.renote()
                            dismiss()
                        },
                        onQuoteRenoteButtonClicked = {
                            notesViewModel.showQuoteNoteEditor(noteId)
                            dismiss()
                        },
                        onRenoteInChannelButtonClicked = {
                            viewModel.renoteToChannel()
                            dismiss()
                        },
                        onQuoteInChannelRenoteButtonClicked = {
                            notesViewModel.showQuoteToChannelNoteEditor(noteId)
                            dismiss()
                        },
                        onDeleteRenoteButtonCLicked = {
                            viewModel.unRenote()
                            dismiss()
                        },
                    )
                }
            }
        }
    }
}

