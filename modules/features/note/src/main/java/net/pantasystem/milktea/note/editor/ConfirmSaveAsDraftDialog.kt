package net.pantasystem.milktea.note.editor

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

class ConfirmSaveAsDraftDialog : DialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "ConfirmSaveAsDraftDialog"
    }

    private val noteEditorViewModel by activityViewModels<NoteEditorViewModel>()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.save_draft)
            .setMessage(R.string.save_the_note_as_a_draft)
            .setPositiveButton(R.string.save) { _, _ ->
                noteEditorViewModel.saveDraft()
                dismiss()
            }
            .setNegativeButton(R.string.delete) { _, _ ->
                requireActivity().finish()
                dismiss()
            }
            .create()
    }
}