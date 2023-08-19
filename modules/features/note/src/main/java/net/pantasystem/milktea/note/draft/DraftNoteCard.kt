package net.pantasystem.milktea.note.draft

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteFile
import net.pantasystem.milktea.note.R


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DraftNoteCard(
    draftNote: DraftNote,
    isVisibleContent: Boolean,
    isPickMode: Boolean,
    onAction: (DraftNoteCardAction) -> Unit,
    onDetach: (DraftNoteFile) -> Unit,
    onShow: (DraftNoteFile) -> Unit,
    onToggleSensitive: (DraftNoteFile) -> Unit,
    onSelect: (DraftNote) -> Unit,
) {

    var confirmDeleteDraftNoteId: Long? by remember {
        mutableStateOf(null)
    }

    fun onConfirmDelete() {
        confirmDeleteDraftNoteId = null
        onAction(DraftNoteCardAction.DeleteDraftNote(draftNote))
    }

    if (confirmDeleteDraftNoteId != null) {
        ConfirmDeleteDraftNoteDialog(
            onDismiss = {
                confirmDeleteDraftNoteId = null
            },
            onConfirmed = {
                onConfirmDelete()
            }
        )
    }
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            if (isPickMode) {
                onSelect(draftNote)
            }
        }
    ) {

        Column(
            Modifier.padding(16.dp)
        ) {
            if (!draftNote.cw.isNullOrBlank()) {
                Text(text = draftNote.cw ?: "")

            }
            AnimatedVisibility(
                visible = isVisibleContent
            ) {
                if (draftNote.text != null) {
                    Text(draftNote.text!!)
                }
            }

            if (draftNote.draftPoll != null) {
                DraftNotePollChoices(choices = draftNote.draftPoll?.choices ?: emptyList())
            }

            if (draftNote.appFiles.isNotEmpty()) {
                HorizontalFilePreviewList(
                    files = draftNote.filePreviewSources,
//                    onAction = {
//                        onAction(DraftNoteCardAction.FileAction(draftNote, it))
//                    },
                    onDetach = {
                        val index = draftNote.filePreviewSources.indexOf(it)
                        onDetach(draftNote.draftFiles!![index])
                    },
                    onShow = {
                        val index = draftNote.filePreviewSources.indexOf(it)
                        onShow(draftNote.draftFiles!![index])
                    },
                    onToggleSensitive = {
                        val index = draftNote.filePreviewSources.indexOf(it)
                        onToggleSensitive(draftNote.draftFiles!![index])
                    }
                )
            }

            if (!isPickMode) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { confirmDeleteDraftNoteId = draftNote.draftNoteId }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_draft_note)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {
                        onAction(DraftNoteCardAction.Edit(draftNote))
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit)
                        )
                    }
                }
            }

        }

    }
}


@Composable
fun DraftNotePollChoices(choices: List<String>) {
    Column {
        choices.forEachIndexed { index, s ->
            DraftNotePollChoice(text = s)
            if (index < choices.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DraftNotePollChoice(text: String) {
    Box(
        modifier = Modifier
            .padding(4.dp)
    ) {
        Surface(
            Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.primary,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(4.dp),
            )
        }

    }

}

@Composable
fun ConfirmDeleteDraftNoteDialog(onDismiss: () -> Unit, onConfirmed: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss.invoke() },
        title = {
            Text(stringResource(id = R.string.confirm_deletion))
        },
        confirmButton = {
            TextButton(onClick = { onConfirmed.invoke() }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss.invoke() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

sealed interface DraftNoteCardAction {
    val draftNote: DraftNote

    data class DeleteDraftNote(override val draftNote: DraftNote) : DraftNoteCardAction
    data class Edit(override val draftNote: DraftNote) : DraftNoteCardAction
}