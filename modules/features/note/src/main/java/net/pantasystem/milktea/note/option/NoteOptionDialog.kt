package net.pantasystem.milktea.note.option

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.report.toReport
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.view.NormalBottomSheetDialogSelectionLayout
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

@AndroidEntryPoint
class NoteOptionDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(noteId: Note.Id): NoteOptionDialog {
            return NoteOptionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(NoteOptionViewModel.NOTE_ID, noteId)
                }
            }
        }
    }
    val viewModel: NoteOptionViewModel by viewModels()
    val notesViewModel: NotesViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                MdcTheme {
                    Surface(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    requireActivity().startActivity(
                                        NoteDetailActivity.newIntent(
                                            requireActivity(),
                                            noteId = uiState.note?.id!!
                                        )
                                    )
                                    dismiss()
                                },
                                icon = Icons.Default.Info,
                                text = stringResource(id = R.string.show_detail)
                            )
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    val clipboardManager =
                                        context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                                    if (clipboardManager == null || uiState.note == null) {
                                        dismiss()
                                    } else {
                                        clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "",
                                                uiState.note?.text
                                            )
                                        )
                                        dismiss()
                                    }

                                },
                                icon = Icons.Default.ContentCopy,
                                text = stringResource(id = R.string.copy_content)
                            )
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    val baseUrl = uiState.currentAccount?.instanceDomain
                                    val url = "$baseUrl/notes/${uiState.note?.id?.noteId}"
                                    val intent = Intent().apply {
                                        action = ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }
                                    startActivity(
                                        Intent.createChooser(
                                            intent,
                                            getString(R.string.share)
                                        )
                                    )
                                    dismiss()
                                },
                                icon = Icons.Default.ContentCopy,
                                text = stringResource(id = R.string.copy_url)
                            )
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    val baseUrl = uiState.currentAccount?.instanceDomain
                                    val url = "$baseUrl/notes/${uiState.note?.id?.noteId}"
                                    val intent = Intent().apply {
                                        action = ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }
                                    startActivity(
                                        Intent.createChooser(
                                            intent,
                                            getString(R.string.share)
                                        )
                                    )
                                    dismiss()
                                },
                                icon = Icons.Default.Share,
                                text = stringResource(id = R.string.share)
                            )
                            Divider()
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    notesViewModel.translate(uiState.noteId!!)
                                    dismiss()
                                },
                                icon = Icons.Default.Translate,
                                text = stringResource(id = R.string.translate)
                            )
                            Divider()
                            if (uiState.noteState?.isFavorited == true) {
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        notesViewModel.deleteFavorite(uiState.noteId!!)
                                        dismiss()
                                    },
                                    icon = Icons.Filled.Star,
                                    text = stringResource(
                                        id = R.string.remove_favorite
                                    )
                                )
                            } else {
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        notesViewModel.addFavorite(uiState.noteId!!)
                                        dismiss()
                                    },
                                    icon = Icons.Outlined.Star,
                                    text = stringResource(id = R.string.favorite)
                                )
                            }

                            if (uiState.isMyNote) {
                                Divider()
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        uiState.noteRelation.let { n ->
                                            notesViewModel.confirmDeleteAndEditEvent.event = n
                                        }
                                        dismiss()
                                    },
                                    icon = Icons.Default.Edit,
                                    text = stringResource(id = R.string.delete_and_edit)
                                )
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        notesViewModel.confirmDeletionEvent.event =
                                            notesViewModel.shareTarget.event
                                    },
                                    icon = Icons.Default.Delete,
                                    text = stringResource(id = R.string.remove_note)
                                )
                            }
                            Divider()
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    val baseUrl = uiState.currentAccount?.instanceDomain

                                    uiState.noteRelation?.let {
                                        val report = it.toReport(baseUrl!!)
                                        notesViewModel.confirmReportEvent.event = report
                                    }
                                    dismiss()
                                },
                                icon = Icons.Default.Report,
                                text = stringResource(id = R.string.report)
                            )
                        }
                    }
                }
            }
        })

        return dialog
    }

}