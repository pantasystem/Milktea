package net.pantasystem.milktea.drive

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.SensitiveIcon
import net.pantasystem.milktea.drive.viewmodel.FileViewData

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Stable
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
            .combinedClickable(
                onClick = {
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
                },
                onLongClick = {
                    onAction(FilePropertyCardAction.OnLongClicked(fileViewData.fileProperty))
                }
            )
    ) {

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (
                fileViewData.fileProperty.thumbnailUrl == null
                || (!fileViewData.fileProperty.type.startsWith("image")
                && !fileViewData.fileProperty.type.startsWith("video"))
            ) {
                Text(fileViewData.fileProperty.name)
            } else {
                Image(
                    rememberAsyncImagePainter(
                        fileViewData.fileProperty.thumbnailUrl
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (fileViewData.fileProperty.isSensitive) {
                SensitiveIcon(modifier = Modifier.align(Alignment.TopStart))
            }

            if (isSelectMode) {
                CircleCheckbox(Modifier.align(Alignment.TopEnd),selected = fileViewData.isSelected) {
                    onAction(
                        FilePropertyCardAction.OnToggleSelectItem(
                            fileViewData.fileProperty.id,
                            !fileViewData.isSelected
                        )
                    )
                }
            }

        }


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
                    FileCardDropdownMenuAction.OnEditFileName -> {
                        onAction(FilePropertyCardAction.OnSelectEditFileNameMenuItem(fileViewData.fileProperty))
                    }
                }
            },
            property = fileViewData.fileProperty
        )
    }
}

@Composable
@Stable
private fun CircleCheckbox(modifier: Modifier = Modifier, selected: Boolean, enabled: Boolean = true, onChecked: () -> Unit) {

    val color = MaterialTheme.colors
    val imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
    val tint = color.primary
    val background = if (selected) Color.White else Color.Transparent

    IconButton(
        onClick = { onChecked() },
        modifier = modifier,
        enabled = enabled
    ) {

        Icon(
            imageVector = imageVector, tint = tint,
            modifier = Modifier.background(background, shape = CircleShape),
            contentDescription = "checkbox"
        )
    }
}
