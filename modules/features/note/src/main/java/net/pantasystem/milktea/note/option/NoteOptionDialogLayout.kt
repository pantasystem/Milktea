package net.pantasystem.milktea.note.option

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.view.NormalBottomSheetDialogSelectionLayout

@Composable
fun NoteOptionDialogLayout(
    uiState: NoteOptionUiState,
    onShowDetailButtonClicked: (Note.Id) -> Unit,
    onCopyTextButtonClicked: (text: Note?) -> Unit,
    onShareButtonClicked: (note: Note?) -> Unit,
    onTranslateButtonClicked: (noteId: Note.Id) -> Unit,
    onDeleteFavoriteButtonClicked: (noteId: Note.Id) -> Unit,
    onCreateFavoriteButtonClicked: (noteId: Note.Id) -> Unit,
    onCreateThreadMuteButtonClicked: (noteId: Note.Id) -> Unit,
    onDeleteThreadMuteButtonClicked: (noteId: Note.Id) -> Unit,
    onDeleteAndEditButtonClicked: (NoteRelation?) -> Unit,
    onDeleteButtonClicked: (NoteRelation?) -> Unit,
    onReportButtonClicked: (NoteRelation?) -> Unit,
) {
    Surface(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            NormalBottomSheetDialogSelectionLayout(
                onClick = {
                    onShowDetailButtonClicked(requireNotNull(uiState.note?.id))
                },
                icon = Icons.Default.Info,
                text = stringResource(id = R.string.show_detail)
            )
            NormalBottomSheetDialogSelectionLayout(
                onClick = {
                    onCopyTextButtonClicked(uiState.note)
                },
                icon = Icons.Default.ContentCopy,
                text = stringResource(id = R.string.copy_content)
            )
            NormalBottomSheetDialogSelectionLayout(
                onClick = {
                    onShareButtonClicked(uiState.note)
                },
                icon = Icons.Default.Share,
                text = stringResource(id = R.string.share)
            )
            Divider()
            NormalBottomSheetDialogSelectionLayout(
                onClick = {
                    onTranslateButtonClicked(requireNotNull(uiState.noteId))
                },
                icon = Icons.Default.Translate,
                text = stringResource(id = R.string.translate)
            )
            Divider()
            if (uiState.noteState?.isFavorited == true) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onDeleteFavoriteButtonClicked(requireNotNull(uiState.noteId))
                    },
                    icon = Icons.Filled.Star,
                    text = stringResource(
                        id = R.string.remove_favorite
                    )
                )
            } else {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onCreateFavoriteButtonClicked(requireNotNull(uiState.noteId))
                    },
                    icon = Icons.Outlined.Star,
                    text = stringResource(id = R.string.favorite)
                )
            }

            if (uiState.noteState?.isMutedThread == true) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onDeleteThreadMuteButtonClicked(requireNotNull(uiState.noteId))
                    },
                    icon = Icons.Default.VolumeMute,
                    text = stringResource(
                        id = R.string.unmute_thread
                    )
                )
            } else {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onCreateThreadMuteButtonClicked(requireNotNull(uiState.noteId))
                    },
                    icon = Icons.Default.VolumeMute,
                    text = stringResource(
                        id = R.string.mute_thread
                    )
                )

            }

            if (uiState.isMyNote) {
                Divider()
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onDeleteAndEditButtonClicked(uiState.noteRelation)
                    },
                    icon = Icons.Default.Edit,
                    text = stringResource(id = R.string.delete_and_edit)
                )
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onDeleteButtonClicked(uiState.noteRelation)
                    },
                    icon = Icons.Default.Delete,
                    text = stringResource(id = R.string.remove_note)
                )
            }
            Divider()
            NormalBottomSheetDialogSelectionLayout(
                onClick = {
                    onReportButtonClicked(uiState.noteRelation)
                },
                icon = Icons.Default.Report,
                text = stringResource(id = R.string.report)
            )
        }
    }
}