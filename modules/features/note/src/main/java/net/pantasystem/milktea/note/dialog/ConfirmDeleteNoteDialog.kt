package net.pantasystem.milktea.note.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

@AndroidEntryPoint
class ConfirmDeleteNoteDialog : DialogFragment() {

    companion object {
        private const val ACCOUNT_ID = "account_id"
        private const val NOTE_ID = "note_id"

        const val FRAGMENT_TAG = "ConfirmDeleteNoteDialog"
        fun newInstance(noteId: Note.Id): ConfirmDeleteNoteDialog {
            return ConfirmDeleteNoteDialog().apply {
                arguments = Bundle().apply {
                    putString(NOTE_ID, noteId.noteId)
                    putLong(ACCOUNT_ID, noteId.accountId)
                }
            }
        }
    }

    private val notesViewModel by activityViewModels<NotesViewModel>()

    private val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong(ACCOUNT_ID),
            requireNotNull(requireArguments().getString(NOTE_ID))
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_deletion)
            .setPositiveButton(R.string.delete) { _, _ ->
                notesViewModel.removeNote(noteId)
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }
}