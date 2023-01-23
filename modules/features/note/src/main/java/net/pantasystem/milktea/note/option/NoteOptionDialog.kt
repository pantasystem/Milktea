package net.pantasystem.milktea.note.option

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.os.Bundle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.report.toReport
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

@AndroidEntryPoint
class NoteOptionDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(noteId: Note.Id, fromPageable: Pageable? = null): NoteOptionDialog {
            return NoteOptionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(NoteOptionViewModel.NOTE_ID, noteId)
                    putSerializable(NoteOptionViewModel.FROM_PAGEABLE, fromPageable)
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
                    NoteOptionDialogLayout(
                        uiState = uiState,
                        onShowDetailButtonClicked = {
                            requireActivity().startActivity(
                                NoteDetailActivity.newIntent(
                                    requireActivity(),
                                    noteId = it,
                                    fromPageable = viewModel.fromPageable
                                )
                            )
                            dismiss()
                        },
                        onCopyTextButtonClicked = {
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
                        onShareButtonClicked = {
                            val baseUrl = uiState.currentAccount?.normalizedInstanceDomain
                            val url = "$baseUrl/notes/${it?.id?.noteId}"
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
                        onTranslateButtonClicked = {
                            notesViewModel.translate(it)
                            dismiss()
                        },
                        onDeleteFavoriteButtonClicked = {
                            notesViewModel.deleteFavorite(it)
                            dismiss()
                        },
                        onCreateFavoriteButtonClicked = {
                            notesViewModel.addFavorite(uiState.noteId!!)
                            dismiss()
                        },
                        onDeleteAndEditButtonClicked = {
                            notesViewModel.confirmDeleteAndEditEvent.event = it
                            dismiss()
                        },
                        onDeleteButtonClicked = {
                            notesViewModel.confirmDeletionEvent.event = it
                            dismiss()
                        },
                        onReportButtonClicked ={
                            val baseUrl = uiState.currentAccount?.normalizedInstanceDomain
                            val report = it?.toReport(baseUrl!!)
                            notesViewModel.confirmReportEvent.event = report
                            dismiss()
                        },
                        onCreateThreadMuteButtonClicked = {
                            viewModel.createThreadMute(it)
                            dismiss()
                        },
                        onDeleteThreadMuteButtonClicked = {
                            viewModel.deleteThreadMute(it)
                            dismiss()
                        },
                        onAddBookmarkButtonClicked = {
                            notesViewModel.addBookmark(it)
                            dismiss()
                        },
                        onDeleteBookmarkButtonClicked = {
                            notesViewModel.removeBookmark(it)
                            dismiss()
                        }
                    )
                }
            }
        })

        return dialog
    }

}