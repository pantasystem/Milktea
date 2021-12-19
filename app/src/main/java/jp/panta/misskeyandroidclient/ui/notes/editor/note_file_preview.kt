package jp.panta.misskeyandroidclient.ui.notes.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.ui.components.HorizontalFilePreviewList
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel

@Composable
fun NoteFilePreview(noteEditorViewModel: NoteEditorViewModel, fileRepository: DriveFileRepository) {
    val files = noteEditorViewModel.files.observeAsState()

    Row (
        verticalAlignment = Alignment.CenterVertically,

    ){
        HorizontalFilePreviewList(
            files = files.value ?: emptyList(),
            repository = fileRepository,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${files.value?.size ?: 0}/4"
        )
    }

}