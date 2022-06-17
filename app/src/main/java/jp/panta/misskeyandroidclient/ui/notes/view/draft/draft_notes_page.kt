package jp.panta.misskeyandroidclient.ui.notes.view.draft

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft.DraftNotesViewModel
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_compose.FilePreviewActionType
import net.pantasystem.milktea.common_compose.FilePreviewTarget
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.notes.draft.DraftNote

@Composable
fun DraftNotesPage(
    viewModel: DraftNotesViewModel,
    filePropertyDataSource: FilePropertyDataSource,
    driveFileRepository: DriveFileRepository,
    onAction: (DraftNotePageAction) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    fun onFileAction(draftNote: DraftNote, action: FilePreviewActionType) {
        val targetFile = draftNote.draftFiles?.firstOrNull { file ->
            AppFile.from(file) == action.target.file
        }?: return
        when (action) {
            is FilePreviewActionType.Detach -> {

                viewModel.detachFile(draftNote, targetFile)
            }
            is FilePreviewActionType.Show -> {
                onAction(DraftNotePageAction.ShowFile(action.target))
            }
            is FilePreviewActionType.ToggleSensitive -> {
                viewModel.toggleSensitive(targetFile)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(DraftNotePageAction.NavigateUp)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.draft_notes))
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        }
    ) {
        Box(
            Modifier
                .padding(it)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.draftNotes is ResultState.Loading) {
                CircularProgressIndicator()
            } else {
                when (val content = state.draftNoteUiStateList.content) {
                    is StateContent.Exist -> {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(
                                content.rawContent,
                                key = { item -> item.draftNote.draftNoteId }) { item ->
                                DraftNoteCard(
                                    draftNote = item.draftNote,
                                    isVisibleContent = item.isVisibleContent,
                                    filePropertyDataSource = filePropertyDataSource,
                                    driveFileRepository = driveFileRepository,
                                    onAction = { action ->
                                        when (action) {
                                            is DraftNoteCardAction.DeleteDraftNote -> {
                                                viewModel.deleteDraftNote(action.draftNote)
                                            }
                                            is DraftNoteCardAction.Edit -> {
                                                onAction(DraftNotePageAction.Edit(action.draftNote))
                                            }
                                            is DraftNoteCardAction.FileAction -> {
                                                onFileAction(item.draftNote, action.fileAction)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    is StateContent.NotExist -> Text("Not exists")
                }
            }

        }
    }
}

sealed interface DraftNotePageAction {
    data class Edit(val draftNote: DraftNote) : DraftNotePageAction
    data class ShowFile(val previewActionType: FilePreviewTarget) : DraftNotePageAction
    object NavigateUp : DraftNotePageAction
}