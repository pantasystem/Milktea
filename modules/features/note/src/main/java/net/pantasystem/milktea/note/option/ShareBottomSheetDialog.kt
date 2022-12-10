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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.user.report.toReport
import net.pantasystem.milktea.note.NoteDetailActivity
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.view.NormalBottomSheetDialogSelectionLayout
import net.pantasystem.milktea.note.viewmodel.NotesViewModel

@AndroidEntryPoint
class ShareBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val viewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        val note = viewModel.shareTarget.event

        dialog.setContentView(ComposeView(requireContext()).apply {
            setContent {
                val noteState by viewModel.shareNoteState.observeAsState()

                MdcTheme {
                    Surface(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    requireActivity().startActivity(
                                        NoteDetailActivity.newIntent(
                                            requireActivity(),
                                            noteId = note?.toShowNote?.note?.id!!
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
                                    if (clipboardManager == null || note == null) {
                                        dismiss()
                                    } else {
                                        clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText(
                                                "",
                                                note.toShowNote.note.text
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
                                    val baseUrl = viewModel.getAccount()?.instanceDomain
                                    val url = "$baseUrl/notes/${note?.id?.noteId}"
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
                                    val baseUrl = viewModel.getAccount()?.instanceDomain
                                    val url = "$baseUrl/notes/${note?.id?.noteId}"
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
                                    viewModel.translate(note?.toShowNote?.note?.id!!)
                                    dismiss()
                                },
                                icon = Icons.Default.Translate,
                                text = stringResource(id = R.string.translate)
                            )
                            Divider()
                            if (noteState?.isFavorited == true) {
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        viewModel.deleteFavorite()
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
                                        viewModel.addFavorite()
                                        dismiss()
                                    },
                                    icon = Icons.Outlined.Star,
                                    text = stringResource(id = R.string.favorite)
                                )
                            }

                            if (note?.isMyNote == true) {
                                Divider()
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        note.let { n ->
                                            viewModel.confirmDeleteAndEditEvent.event = n
                                        }
                                        dismiss()
                                    },
                                    icon = Icons.Default.Edit,
                                    text = stringResource(id = R.string.delete_and_edit)
                                )
                                NormalBottomSheetDialogSelectionLayout(
                                    onClick = {
                                        viewModel.confirmDeletionEvent.event =
                                            viewModel.shareTarget.event
                                    },
                                    icon = Icons.Default.Delete,
                                    text = stringResource(id = R.string.remove_note)
                                )
                            }
                            Divider()
                            NormalBottomSheetDialogSelectionLayout(
                                onClick = {
                                    val baseUrl = viewModel.getAccount()?.instanceDomain

                                    note?.let {
                                        val report = it.toShowNote.toReport(baseUrl!!)
                                        viewModel.confirmReportEvent.event = report
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