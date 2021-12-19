package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.ui.components.HorizontalFilePreviewList
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryEditorViewModel

@Composable
fun PickedImagePreview(
    viewModel: GalleryEditorViewModel,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource
) {
    val files = viewModel.pickedImages.observeAsState()
    HorizontalFilePreviewList(
        files = files.value ?: emptyList(),
        repository = repository,
        dataSource = dataSource,
        onAction = {}
    )
}