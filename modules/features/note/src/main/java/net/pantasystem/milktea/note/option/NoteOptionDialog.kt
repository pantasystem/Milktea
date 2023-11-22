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
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_compose.haptic.rememberHapticFeedback
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.report.toReport
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.clip.ToggleAddNoteToClipDialog
import net.pantasystem.milktea.note.reaction.history.ReactionHistoryPagerDialog
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NoteOptionDialog : BottomSheetDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "NoteOptionDialog"
        fun newInstance(noteId: Note.Id, fromPageable: Pageable? = null): NoteOptionDialog {
            return NoteOptionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(NoteOptionViewModel.NOTE_ID, noteId)
                    putSerializable(NoteOptionViewModel.FROM_PAGEABLE, fromPageable)
                }
            }
        }
    }

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    val viewModel: NoteOptionViewModel by viewModels()
    val notesViewModel: NotesViewModel by activityViewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                    val feedback = rememberHapticFeedback()
                    NoteOptionDialogLayout(
                        uiState = uiState,
                        onShowDetailButtonClicked = {
                            feedback.performClickHapticFeedback()
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
                            feedback.performClickHapticFeedback()
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
                            feedback.performClickHapticFeedback()
                            val baseUrl = uiState.currentAccount?.normalizedInstanceUri
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
                            feedback.performClickHapticFeedback()
                            notesViewModel.translate(it)
                            dismiss()
                        },
                        onDeleteFavoriteButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.deleteFavorite(it)
                            dismiss()
                        },
                        onCreateFavoriteButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.addFavorite(uiState.noteId!!)
                            dismiss()
                        },
                        onDeleteAndEditButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.confirmDeleteAndEditEvent.tryEmit(it)
                            dismiss()
                        },
                        onDeleteButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.confirmDeletionEvent.tryEmit(it)
                            dismiss()
                        },
                        onReportButtonClicked ={
                            if (it != null) {
                                feedback.performClickHapticFeedback()
                                val baseUrl = uiState.currentAccount?.normalizedInstanceUri
                                val report = it.toReport(baseUrl!!)
                                notesViewModel.confirmReportEvent.tryEmit(report)
                            }
                            dismiss()
                        },
                        onCreateThreadMuteButtonClicked = {
                            feedback.performClickHapticFeedback()
                            viewModel.createThreadMute(it)
                            dismiss()
                        },
                        onDeleteThreadMuteButtonClicked = {
                            feedback.performClickHapticFeedback()
                            viewModel.deleteThreadMute(it)
                            dismiss()
                        },
                        onAddBookmarkButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.addBookmark(it)
                            dismiss()
                        },
                        onDeleteBookmarkButtonClicked = {
                            feedback.performClickHapticFeedback()
                            notesViewModel.removeBookmark(it)
                            dismiss()
                        },
                        onShowReactionHistoryButtonClicked = {
                            feedback.performClickHapticFeedback()
                            dismiss()
                            ReactionHistoryPagerDialog.newInstance(it).show(parentFragmentManager, ReactionHistoryPagerDialog.FRAGMENT_TAG)
                        },
                        onToggleAddNoteToClipButtonClicked = {
                            feedback.performClickHapticFeedback()
                            dismiss()
                            ToggleAddNoteToClipDialog.newInstance(it).show(parentFragmentManager, ToggleAddNoteToClipDialog.FRAGMENT_TAG)
                        }
                    )
                }
            }
        })

        return dialog
    }

}