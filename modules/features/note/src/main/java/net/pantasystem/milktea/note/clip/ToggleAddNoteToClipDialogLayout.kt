package net.pantasystem.milktea.note.clip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.clip.Clip
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ToggleAddNoteToClipDialogLayout(
    uiState: ToggleAddNoteToClipDialogUiState,
    onAddNoteToClip: (Note.Id, Clip) -> Unit,
    onRemoveNoteToClip: (Note.Id, Clip) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.clip), fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .nestedScroll(rememberNestedScrollInteropConnection())
            ) {
                when(val state = uiState.clips) {
                    is ResultState.Error -> {
                        item {
                            Text("load error")
                        }
                    }
                    is ResultState.Fixed -> {
                        when (val content = state.content) {
                            is StateContent.Exist -> {
                                items(content.rawContent) { clip ->
                                    ToggleAddNoteToClipTile(
                                        clip = clip.clip,
                                        state = clip.addState,
                                        onClick = {
                                            when(clip.addState) {
                                                ClipAddState.Added -> {
                                                    uiState.noteId?.let {
                                                        onRemoveNoteToClip(it, clip.clip)
                                                    }
                                                }
                                                ClipAddState.Unknown, ClipAddState.NotAdded -> {
                                                    uiState.noteId?.let {
                                                        onAddNoteToClip(it, clip.clip)
                                                    }
                                                }
                                                ClipAddState.Progress -> {}
                                            }
                                        }
                                    )
                                }
                            }
                            is StateContent.NotExist -> {
                                item {
                                    Text("clip is not exists")
                                }
                            }
                        }
                    }
                    is ResultState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}