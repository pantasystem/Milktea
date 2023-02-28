package net.pantasystem.milktea.note.clip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.Note

@AndroidEntryPoint
class ToggleAddNoteToClipDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(noteId: Note.Id): ToggleAddNoteToClipDialog {
            return ToggleAddNoteToClipDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ToggleAddNoteToClipDialogViewModel.EXTRA_NOTE_ID, noteId)
                }
            }
        }
    }

    private val viewModel: ToggleAddNoteToClipDialogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(
            requireContext()
        ).apply {
            setContent {
                MdcTheme {
                    val uiState by viewModel.uiState.collectAsState()
                    ToggleAddNoteToClipDialogLayout(
                        uiState = uiState,
                        onAddNoteToClip = viewModel::onAddToClip,
                        onRemoveNoteToClip = viewModel::onRemoveToClip
                    )
                }
            }
        }
    }
}