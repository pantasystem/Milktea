package net.pantasystem.milktea.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import net.pantasystem.milktea.common_compose.HorizontalFilePreviewList
import net.pantasystem.milktea.gallery.viewmodel.GalleryEditorViewModel
import net.pantasystem.milktea.model.file.FilePreviewSource

@Composable
fun PickedImagePreview(
    viewModel: GalleryEditorViewModel,
    onShow: (FilePreviewSource) -> Unit,
) {
    val files = viewModel.pickedImages.collectAsState()
    HorizontalFilePreviewList(
        files = files.value,
        onShow = {
            onShow(it)
        },
        onToggleSensitive = {
            viewModel.toggleSensitive(it.file)
        },
        onDetach = {
            viewModel.detach(it.file)
        }
    )
}