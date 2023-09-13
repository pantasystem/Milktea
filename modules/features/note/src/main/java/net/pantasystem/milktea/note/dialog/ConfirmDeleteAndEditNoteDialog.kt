package net.pantasystem.milktea.note.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

class ConfirmDeleteAndEditNoteDialog : DialogFragment() {

    companion object {
        private const val ACCOUNT_ID = "account_id"
        private const val NOTE_ID = "note_id"

        const val FRAGMENT_TAG = "ConfirmDeleteAndDeleteNoteDialog"
        fun newInstance(noteId: Note.Id): ConfirmDeleteAndEditNoteDialog {
            return ConfirmDeleteAndEditNoteDialog().apply {
                arguments = Bundle().apply {
                    putString(NOTE_ID, noteId.noteId)
                    putLong(ACCOUNT_ID, noteId.accountId)
                }
            }
        }
    }

    private val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong(ACCOUNT_ID),
            requireNotNull(requireArguments().getString(NOTE_ID))
        )
    }

    private val notesViewModel by activityViewModels<NotesViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.confirm_delete_and_edit_note_description)
            .setPositiveButton(R.string.delete_and_edit) { _, _ ->
                notesViewModel.removeAndEditNote(noteId)
                dismiss()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()
    }
}