package net.pantasystem.milktea.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import net.pantasystem.milktea.common_compose.FilePreviewActionType
import net.pantasystem.milktea.common_compose.FilePreviewSource
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.gallery.viewmodel.GalleryEditorViewModel
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource

@Composable
fun PickedImagePreview(
    viewModel: GalleryEditorViewModel,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onShow: (FilePreviewSource) -> Unit,
) {
    val files = viewModel.pickedImages.collectAsState()
    HorizontalFilePreviewList(
        files = files.value,
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