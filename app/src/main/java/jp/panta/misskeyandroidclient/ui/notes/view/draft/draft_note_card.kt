package jp.panta.misskeyandroidclient.ui.notes.view.draft

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
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.common_compose.FilePreviewActionType
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.draft.DraftNote


@Composable
fun DraftNoteCard(
    draftNote: DraftNote,
    isVisibleContent: Boolean,
    filePropertyDataSource: FilePropertyDataSource,
    driveFileRepository: DriveFileRepository,
    onAction: (DraftNoteCardAction) -> Unit,
) {

    var confirmDeleteDraftNoteId: Long? by remember {
        mutableStateOf(null)
    }

//    fun onConfirmDelete() {
//        confirmDeleteDraftNoteId = null
////        onAction(DraftNoteCardAction.DeleteDraftNote(draftNote))
//    }

//    if (confirmDeleteDraftNoteId != null) {
//        AlertDialog(
//            onDismissRequest = { confirmDeleteDraftNoteId = null },
//            title = {
//                Text(stringResource(id = R.string.confirm_deletion))
//            },
//            confirmButton = {
//                TextButton(onClick = ::onConfirmDelete) {
//                    Text(stringResource(R.string.delete))
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { confirmDeleteDraftNoteId = null }) {
//                    Text(stringResource(R.string.cancel))
//                }
//            }
//        )
//    }
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
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
                    files = draftNote.appFiles,
                    repository = driveFileRepository,
                    dataSource = filePropertyDataSource,
                    onAction = {
//                        onAction(DraftNoteCardAction.FileAction(draftNote, it))
                    },
                )
            }

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

sealed interface DraftNoteCardAction {
    val draftNote: DraftNote

    data class FileAction(
        override val draftNote: DraftNote,
        val fileAction: FilePreviewActionType
    ) : DraftNoteCardAction

    data class DeleteDraftNote(override val draftNote: DraftNote) : DraftNoteCardAction
    data class Edit(override val draftNote: DraftNote) : DraftNoteCardAction
}