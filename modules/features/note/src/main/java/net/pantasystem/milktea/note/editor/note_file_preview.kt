package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@ExperimentalCoroutinesApi
@Composable
fun NoteFilePreview(
    noteEditorViewModel: NoteEditorViewModel,
    onShow: (FilePreviewSource) -> Unit,
    onEditFileNameSelectionClicked: (FilePreviewSource) -> Unit,
    onEditFileCaptionSelectionClicked: (FilePreviewSource) -> Unit,
) {
    val uiState by noteEditorViewModel.uiState.collectAsState()
    val maxFileCount = noteEditorViewModel.maxFileCount.asLiveData().observeAsState()
    val instanceInfo by noteEditorViewModel.instanceInfo.collectAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalFilePreviewList(
            files = uiState.files,
            modifier = Modifier.weight(1f),
            allowMaxFileSize = instanceInfo?.clientMaxBodyByteSize,
            onToggleSensitive = {
                noteEditorViewModel.toggleNsfw(it.file)
            },
            onDetach = {
                noteEditorViewModel.removeFileNoteEditorData(it.file)
            },
            onShow = onShow,
            onEditFileName = onEditFileNameSelectionClicked,
            onEditFileCaption = onEditFileCaptionSelectionClicked
        )
        if (uiState.files.isNotEmpty())
            Text(
                "${uiState.files.size}/${maxFileCount.value}"
            )
    }

}