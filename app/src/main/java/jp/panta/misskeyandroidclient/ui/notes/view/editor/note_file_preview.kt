package jp.panta.misskeyandroidclient.ui.notes.view.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.ui.components.FilePreviewActionType
import jp.panta.misskeyandroidclient.ui.components.FilePreviewTarget
import jp.panta.misskeyandroidclient.ui.components.HorizontalFilePreviewList
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@Composable
fun NoteFilePreview(
    noteEditorViewModel: NoteEditorViewModel,
    fileRepository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onShow: (FilePreviewTarget)->Unit
) {
    val files = noteEditorViewModel.files.observeAsState()
    val maxFileCount = noteEditorViewModel.maxFileCount.asLiveData().observeAsState()

    Row (
        verticalAlignment = Alignment.CenterVertically,
    ){
        HorizontalFilePreviewList(
            files = files.value ?: emptyList(),
            repository = fileRepository,
            modifier = Modifier.weight(1f),
            dataSource = dataSource,
            onAction = {
                when(it) {
                    is FilePreviewActionType.ToggleSensitive -> {
                        noteEditorViewModel.toggleNsfw(it.target.file)
                    }
                    is FilePreviewActionType.Detach -> {
                        noteEditorViewModel.removeFileNoteEditorData(it.target.file)
                    }
                    is FilePreviewActionType.Show -> {
                        onShow(it.target)
                    }
                }
            }
        )
        Text(
            "${files.value?.size ?: 0}/${maxFileCount.value}"
        )
    }

}