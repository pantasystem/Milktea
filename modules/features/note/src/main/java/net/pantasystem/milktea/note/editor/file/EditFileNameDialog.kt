package net.pantasystem.milktea.note.editor.file

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@Suppress("DEPRECATION")
@AndroidEntryPoint
class EditFileNameDialog : AppCompatDialogFragment() {

    companion object {
        fun newInstance(appFile: AppFile, name: String?): EditFileNameDialog {
            return EditFileNameDialog().apply {
                arguments = Bundle().apply {
                    putSerializable("EXTRA_APP_FILE", appFile)
                    putString("EXTRA_NAME", name)
                }
            }
        }
    }

    val noteEditorViewModel by activityViewModels<NoteEditorViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val appFile = requireArguments().getSerializable("EXTRA_APP_FILE") as AppFile

        val view = ComposeView(requireContext()).apply {
            setContent {
                var text: String by remember {
                    mutableStateOf(requireArguments().getString("EXTRA_NAME") ?: "")
                }
                MdcTheme {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colors.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                stringResource(id = R.string.edit_file_name),
                                fontSize = 24.sp,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = text,
                                placeholder = {
                                    Text(stringResource(R.string.input_caption))
                                },
                                onValueChange = { t ->
                                    text = t
                                }
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    dismiss()
                                }) {
                                    Text(stringResource(id = R.string.cancel))
                                }
                                TextButton(onClick = {
                                    noteEditorViewModel.updateFileName(appFile, text)
                                    dismiss()
                                }) {
                                    Text(stringResource(id = R.string.save))
                                }
                            }
                        }
                    }
                }
            }
        }
        dialog.setContentView(view)
        return dialog
    }
}