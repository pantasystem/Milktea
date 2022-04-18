package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.ui.components.FilePreviewActionType
import jp.panta.misskeyandroidclient.ui.components.FilePreviewTarget
import jp.panta.misskeyandroidclient.ui.components.HorizontalFilePreviewList
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryEditorViewModel

@Composable
fun PickedImagePreview(
    viewModel: GalleryEditorViewModel,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onShow: (FilePreviewTarget) -> Unit,
) {
    val files = viewModel.pickedImages.observeAsState()
    HorizontalFilePreviewList(
        files = files.value ?: emptyList(),
        repository = repository,
        dataSource = dataSource,
        onAction = {
            when(it) {
                is FilePreviewActionType.ToggleSensitive -> {
                    viewModel.toggleSensitive(it.target.file)
                }
                is FilePreviewActionType.Show -> {
                    onShow(it.target)
                }
                is FilePreviewActionType.Detach -> {
                    viewModel.detach(it.target.file)
                }
            }
        }
    )
}