package net.pantasystem.milktea.note.editor

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.convertToHumanReadable
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class NoteEditorFileSizeWarningDialog : AppCompatDialogFragment() {

    private val noteEditorViewModel: NoteEditorViewModel by activityViewModels()

    companion object {
        const val FRAGMENT_TAG = "NoteEditorFileSizeWarningDialog"
        private const val EXTRA_APP_FILE_LOCAL = "NoteEditorFileSizeWarningDialog.EXTRA_APP_FILE_LOCAL"
        private const val EXTRA_HOST = "NoteEditorFileSizeWarningDialog.EXTRA_HOST"
        private const val EXTRA_ALLOW_MAX_SIZE = "NoteEditorFileSizeWarningDialog.EXTRA_ALLOW_MAX_SIZE"
        fun newInstance(host: String, allowFileMaxSize: Long, file: AppFile.Local): NoteEditorFileSizeWarningDialog {
            return NoteEditorFileSizeWarningDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_APP_FILE_LOCAL, file)
                    putString(EXTRA_HOST, host)
                    putLong(EXTRA_ALLOW_MAX_SIZE, allowFileMaxSize)
                }
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val appFile = requireNotNull(requireArguments().getSerializable(EXTRA_APP_FILE_LOCAL) as AppFile.Local)
        val host = requireNotNull(requireArguments().getString(EXTRA_HOST))
        val allowSize = requireNotNull(requireArguments().getLong(EXTRA_ALLOW_MAX_SIZE))

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.file_size_warning_dialog_title)
            .setMessage(getString(R.string.file_size_warning_dialog_message, host, convertHumanFriendly(allowSize)))
            .setPositiveButton(R.string.file_size_warning_dialo_positive_button) { _, _ ->
                dismiss()
            }.setNegativeButton(R.string.file_size_warning_dialog_negative_button) { _, _ ->
                noteEditorViewModel.removeFileNoteEditorData(appFile)
                dismiss()
            }.create()

    }

    private fun convertHumanFriendly(size: Long?): String {
        size ?: return "0B"

        return size.convertToHumanReadable()

    }
}