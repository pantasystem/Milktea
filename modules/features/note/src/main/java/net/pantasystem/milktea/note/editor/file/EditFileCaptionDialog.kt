package net.pantasystem.milktea.note.editor.file

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_compose.drive.EditCaptionDialogLayout
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class EditFileCaptionDialog : AppCompatDialogFragment() {

    companion object {
        fun newInstance(appFile: AppFile, comment: String?): EditFileCaptionDialog {
            return EditFileCaptionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable("EXTRA_APP_FILE", appFile)
                    putString("EXTRA_COMMENT", comment)
                }
            }
        }
    }

    val noteEditorViewModel by activityViewModels<NoteEditorViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val appFile = requireArguments().getSerializable("EXTRA_APP_FILE") as AppFile

        val comment = requireArguments().getString("EXTRA_COMMENT") ?: ""
        val view = ComposeView(requireContext()).apply {
            setContent {
                var text: String by remember(comment) {
                    mutableStateOf(comment)
                }
                MdcTheme {
                    EditCaptionDialogLayout(
                        value = text,
                        onCancelButtonClicked = {
                            dismiss()
                        },
                        onTextChanged = {
                            text = it
                        },
                        onSaveButtonClicked = {
                            noteEditorViewModel.updateFileComment(appFile, text)
                            dismiss()
                        }
                    )
                }
            }
        }
        dialog.setContentView(view)
        return dialog
    }
}