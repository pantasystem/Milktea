package net.pantasystem.milktea.drive

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import net.pantasystem.milktea.common_compose.drive.EditCaptionDialogLayout
import net.pantasystem.milktea.model.drive.FileProperty

@Composable
@Stable
fun FileActionDropdownMenu(
    property: FileProperty,
    expanded: Boolean,
    onAction: (FileCardDropdownMenuAction) -> Unit
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            onAction(FileCardDropdownMenuAction.OnDismissRequest)
        },
        modifier = Modifier.wrapContentWidth(),
    ) {
        DropdownMenuItem(
            onClick = {
                onAction(FileCardDropdownMenuAction.OnNsfwMenuItemClicked)
            }
        ) {
            if (property.isSensitive) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = stringResource(R.string.undo_nsfw),
                    modifier = Modifier.size(24.dp)

                )
                Text(stringResource(R.string.undo_nsfw))
            } else {
                Icon(
                    Icons.Default.HideImage,
                    contentDescription = stringResource(R.string.mark_as_nsfw),
                    modifier = Modifier.size(24.dp)
                )
                Text(stringResource(R.string.mark_as_nsfw))
            }
        }

        Divider()
        DropdownMenuItem(
            onClick = {
                onAction(FileCardDropdownMenuAction.OnDeleteMenuItemClicked)
            },
        ) {
            Icon(
                Icons.Default.Delete,
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.delete)
            )
            Text(text = stringResource(R.string.delete))
        }
        Divider()
        DropdownMenuItem(onClick = {
            onAction(FileCardDropdownMenuAction.OnEditFileCaption)
        }) {
            Icon(
                Icons.Default.Edit,
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.edit_caption)
            )
            Text(text = stringResource(R.string.edit_caption))
        }
    }


}

@Composable
fun ConfirmDeleteFilePropertyDialog(
    isShow: Boolean,
    filename: String,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
    if (isShow) {
        AlertDialog(
            onDismissRequest = onDismissRequest,

            title = {
                Text(stringResource(R.string.file_deletion_confirmation))
            },
            confirmButton = {
                TextButton(onClick = onConfirmed) {
                    Text(stringResource(R.string.delete))
                }
            },
            text = {
                Text(stringResource(R.string.do_u_want_2_delete_s, filename))
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

}


@Composable
fun EditCaptionDialog(
    fileProperty: FileProperty?,
    onDismiss: () -> Unit,
    onSave: (FileProperty.Id, newCaption: String) -> Unit
) {

    var captionText: String by remember(fileProperty) {
        mutableStateOf(fileProperty?.comment ?: "")
    }
    if (fileProperty != null) {
        Dialog(onDismissRequest = onDismiss) {
            EditCaptionDialogLayout(value = captionText, onCancelButtonClicked = onDismiss, onTextChanged = {
                captionText = it
            }, onSaveButtonClicked = {
                onSave(fileProperty.id, captionText)
            })
        }
    }

}


sealed interface FileCardDropdownMenuAction {
    object OnDismissRequest : FileCardDropdownMenuAction
    object OnNsfwMenuItemClicked : FileCardDropdownMenuAction
    object OnDeleteMenuItemClicked : FileCardDropdownMenuAction
    object OnEditFileCaption : FileCardDropdownMenuAction
}