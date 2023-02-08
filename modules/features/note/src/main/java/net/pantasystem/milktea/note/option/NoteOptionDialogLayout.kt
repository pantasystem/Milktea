package net.pantasystem.milktea.note.option

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.model.account.Account
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
    onDeleteBookmarkButtonClicked: (noteId: Note.Id) -> Unit,
    onAddBookmarkButtonClicked: (noteId: Note.Id) -> Unit,
    onCreateFavoriteButtonClicked: (noteId: Note.Id) -> Unit,
    onCreateThreadMuteButtonClicked: (noteId: Note.Id) -> Unit,
    onDeleteThreadMuteButtonClicked: (noteId: Note.Id) -> Unit,
    onDeleteAndEditButtonClicked: (NoteRelation?) -> Unit,
    onDeleteButtonClicked: (NoteRelation?) -> Unit,
    onReportButtonClicked: (NoteRelation?) -> Unit,
    onShowReactionHistoryButtonClicked: (noteId: Note.Id) -> Unit,
    onToggleAddNoteToClipButtonClicked: (noteId: Note.Id) -> Unit,
) {
    Surface(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            if (uiState.note?.id != null) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onShowDetailButtonClicked(uiState.note.id)
                    },
                    icon = Icons.Default.Info,
                    text = stringResource(id = R.string.show_detail)
                )
            }

            if (uiState.currentAccount?.instanceType == Account.InstanceType.MISSKEY
                && uiState.note?.id != null
            ) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onShowReactionHistoryButtonClicked(uiState.note.id)
                    },
                    icon = Icons.Default.Mood, text = stringResource(
                        id = R.string.reaction
                    )
                )
            }

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
            if (uiState.noteId != null) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = {
                        onTranslateButtonClicked(uiState.noteId)
                    },
                    icon = Icons.Default.Translate,
                    text = stringResource(id = R.string.translate)
                )
            }
            Divider()
            if (uiState.note?.isMisskey == true) {
                if (uiState.noteState?.isFavorited == true) {
                    if (uiState.noteId != null) {
                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onDeleteFavoriteButtonClicked(uiState.noteId)
                            },
                            icon = Icons.Filled.Star,
                            text = stringResource(
                                id = R.string.remove_favorite
                            )
                        )
                    }
                } else {
                    if (uiState.noteId != null) {
                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onCreateFavoriteButtonClicked(uiState.noteId)
                            },
                            icon = Icons.Outlined.Star,
                            text = stringResource(id = R.string.favorite)
                        )
                    }
                }
            } else {
                when ((uiState.note?.type as? Note.Type.Mastodon)?.bookmarked) {
                    true -> {
                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onDeleteBookmarkButtonClicked(uiState.note.id)
                            },
                            icon = Icons.Filled.BookmarkRemove,
                            text = stringResource(
                                id = R.string.remove_bookmark
                            )
                        )
                    }
                    false -> {
                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onAddBookmarkButtonClicked(uiState.note.id)
                            },
                            icon = Icons.Outlined.BookmarkAdd,
                            text = stringResource(id = R.string.add_to_bookmark)
                        )
                    }
                    else -> {}
                }
            }

            if (uiState.note?.isMisskey == true) {
                NormalBottomSheetDialogSelectionLayout(
                    icon = Icons.Default.Attachment, text = stringResource(id = R.string.clip),
                    onClick = {
                        onToggleAddNoteToClipButtonClicked(
                            uiState.note.id
                        )
                    },
                )
            }

            if (uiState.noteState == null || uiState.noteState.isMutedThread != null) {
                if (uiState.noteState?.isMutedThread == true) {
                    if (uiState.noteId != null) {
                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onDeleteThreadMuteButtonClicked(uiState.noteId)
                            },
                            icon = Icons.Default.VolumeMute,
                            text = stringResource(
                                id = R.string.unmute_thread
                            )
                        )
                    }
                } else {
                    if (uiState.noteId != null) {

                        NormalBottomSheetDialogSelectionLayout(
                            onClick = {
                                onCreateThreadMuteButtonClicked(uiState.noteId)
                            },
                            icon = Icons.Default.VolumeMute,
                            text = stringResource(
                                id = R.string.mute_thread
                            )
                        )
                    }
                }
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