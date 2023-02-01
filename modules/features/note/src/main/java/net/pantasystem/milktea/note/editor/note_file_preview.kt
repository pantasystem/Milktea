package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.note.R
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
    val instanceInfoType by noteEditorViewModel.instanceInfoType.collectAsState()
    val isSensitive by noteEditorViewModel.isSensitiveMedia.collectAsState()

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalFilePreviewList(
                files = uiState.files,
                modifier = Modifier.weight(1f),
                isMisskey = instanceInfoType is InstanceInfoType.Misskey,
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
        if (instanceInfoType is InstanceInfoType.Mastodon && uiState.files.isNotEmpty()) {
            SwitchTile(
                modifier = Modifier.padding(horizontal = 16.dp),
                checked = isSensitive ?: false,
                onChanged = {
                    noteEditorViewModel.toggleSensitive()
                }
            ) {
                Text(stringResource(id = R.string.mark_media_as_sensitive))
            }
        }
    }


}