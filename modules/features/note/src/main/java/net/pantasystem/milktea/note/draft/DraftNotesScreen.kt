package net.pantasystem.milktea.note.draft

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
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteFile
import net.pantasystem.milktea.note.draft.viewmodel.DraftNotesViewModel

@Composable
fun DraftNotesScreen(
    viewModel: DraftNotesViewModel,
    onShowFile: (DraftNoteFile) -> Unit,
    onNavigateUp: () -> Unit,
    onEdit: (DraftNote) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(text = stringResource(id = net.pantasystem.milktea.common_resource.R.string.draft_notes))
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .padding(paddingValues)
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
                                    onAction = { action ->
                                        when (action) {
                                            is DraftNoteCardAction.DeleteDraftNote -> {
                                                viewModel.deleteDraftNote(action.draftNote)
                                            }
                                            is DraftNoteCardAction.Edit -> {
                                                onEdit(action.draftNote)
                                            }

                                        }
                                    },
                                    onShow = onShowFile,
                                    onDetach = { e ->
                                        viewModel.detachFile(item.draftNote, e)
                                    },
                                    onToggleSensitive = { e ->
                                        viewModel.toggleSensitive(e)
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