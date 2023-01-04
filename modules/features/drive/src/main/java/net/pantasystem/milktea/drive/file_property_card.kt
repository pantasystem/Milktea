package net.pantasystem.milktea.drive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.SensitiveIcon
import net.pantasystem.milktea.drive.viewmodel.FileViewData
import net.pantasystem.milktea.model.drive.FileProperty


@ExperimentalMaterialApi
@Composable
fun FilePropertySimpleCard(
    file: FileViewData,
    isSelectMode: Boolean = false,
    onAction: (FilePropertyCardAction) -> Unit,
) {

    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = if (file.isSelected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        },
        onClick = {
            if (isSelectMode) {
                onAction(FilePropertyCardAction.OnToggleSelectItem(file.fileProperty.id, !file.isSelected))
            } else {
                onAction(FilePropertyCardAction.OnOpenDropdownMenu(file.fileProperty.id))
            }

        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp)
                        .padding(end = 4.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            file.fileProperty.thumbnailUrl
                                ?: file.fileProperty.url
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .height(64.dp)
                            .width(64.dp),
                        contentScale = ContentScale.Crop
                    )
                    if (file.fileProperty.isSensitive) {
                        SensitiveIcon()
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        file.fileProperty.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Row {
                        Text(
                            file.fileProperty.type,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            file.fileProperty.size.toString()
                        )
                    }
                }


            }
            Box(
                modifier = Modifier.align(Alignment.End)
            ) {
                FileActionDropdownMenu(

                    expanded = file.isDropdownMenuExpanded,
                    onAction = { e ->
                        onAction(FilePropertyCardAction.OnCloseDropdownMenu(file.fileProperty.id))
                        when(e) {
                            FileCardDropdownMenuAction.OnDeleteMenuItemClicked -> {
                                onAction(FilePropertyCardAction.OnSelectDeletionMenuItem(file.fileProperty))
                            }
                            FileCardDropdownMenuAction.OnDismissRequest -> {
                            }
                            FileCardDropdownMenuAction.OnEditFileCaption -> {
                                onAction(FilePropertyCardAction.OnSelectEditCaptionMenuItem(file.fileProperty))
                            }
                            FileCardDropdownMenuAction.OnNsfwMenuItemClicked -> {
                                onAction(FilePropertyCardAction.OnToggleNsfw(file.fileProperty.id))
                            }
                            FileCardDropdownMenuAction.OnEditFileName -> {
                                onAction(FilePropertyCardAction.OnSelectEditFileNameMenuItem(file.fileProperty))
                            }
                        }
                    },
                    property = file.fileProperty
                )
            }

        }


    }




}

sealed interface FilePropertyCardAction {
    data class OnOpenDropdownMenu(val fileId: FileProperty.Id) : FilePropertyCardAction
    data class OnCloseDropdownMenu(val fileId: FileProperty.Id) : FilePropertyCardAction
    data class OnToggleSelectItem(val fileId: FileProperty.Id, val newValue: Boolean) : FilePropertyCardAction
    data class OnToggleNsfw(val fileId: FileProperty.Id) : FilePropertyCardAction
    data class OnSelectDeletionMenuItem(val file: FileProperty) : FilePropertyCardAction
    data class OnSelectEditCaptionMenuItem(val file: FileProperty) : FilePropertyCardAction
    data class OnSelectEditFileNameMenuItem(val file: FileProperty) : FilePropertyCardAction
}
