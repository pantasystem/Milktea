package net.pantasystem.milktea.drive

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.drive.viewmodel.FileViewData

@Composable
fun FilePropertyGridItem(
    fileViewData: FileViewData,
    isSelectMode: Boolean = false,
    onAction: (FilePropertyCardAction) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
            .aspectRatio(1f)
            .clickable {
                if (isSelectMode) {
                    onAction(
                        FilePropertyCardAction.OnToggleSelectItem(
                            fileViewData.fileProperty.id,
                            !fileViewData.isSelected
                        )
                    )
                } else {
                    onAction(FilePropertyCardAction.OnOpenDropdownMenu(fileViewData.fileProperty.id))
                }
            }
    ) {

        Image(
            rememberAsyncImagePainter(
                fileViewData.fileProperty.thumbnailUrl
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        FileActionDropdownMenu(
            expanded = fileViewData.isDropdownMenuExpanded,
            onAction = { e ->
                onAction(FilePropertyCardAction.OnCloseDropdownMenu(fileViewData.fileProperty.id))
                when (e) {
                    FileCardDropdownMenuAction.OnDeleteMenuItemClicked -> {
                        onAction(FilePropertyCardAction.OnSelectDeletionMenuItem(fileViewData.fileProperty))
                    }
                    FileCardDropdownMenuAction.OnDismissRequest -> {
                    }
                    FileCardDropdownMenuAction.OnEditFileCaption -> {
                        onAction(FilePropertyCardAction.OnSelectEditCaptionMenuItem(fileViewData.fileProperty))
                    }
                    FileCardDropdownMenuAction.OnNsfwMenuItemClicked -> {
                        onAction(FilePropertyCardAction.OnToggleNsfw(fileViewData.fileProperty.id))
                    }
                }
            },
            property = fileViewData.fileProperty
        )
    }
}