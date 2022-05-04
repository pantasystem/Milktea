package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.drive.FileProperty

@Composable
fun FileActionDropdownMenu(
    property: FileProperty,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNsfwMenuItemClicked: () -> Unit,
    onDeleteMenuItemClicked: () -> Unit,
    onEditFileCaption: () -> Unit,
) {


    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentWidth(),
    ) {
        DropdownMenuItem(
            onClick = onNsfwMenuItemClicked
        ) {
            if (property.isSensitive) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_image_24
                    ),
                    contentDescription = stringResource(R.string.undo_nsfw),
                    modifier = Modifier.size(24.dp)

                )
                Text(stringResource(R.string.undo_nsfw))
            } else {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_hide_image_24
                    ),
                    contentDescription = stringResource(R.string.mark_as_nsfw),
                    modifier = Modifier.size(24.dp)
                )
                Text(stringResource(R.string.mark_as_nsfw))
            }
        }

        Divider()
        DropdownMenuItem(
            onClick = onDeleteMenuItemClicked,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_black_24dp),
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.delete)
            )
            Text(text = stringResource(R.string.delete))
        }
        Divider()
        DropdownMenuItem(onClick = onEditFileCaption) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_black_24dp),
                modifier = Modifier.size(24.dp),
                contentDescription = stringResource(R.string.edit_caption)
            )
            Text(text = stringResource(R.string.edit_caption))
        }
    }


}

@Composable
fun ConfirmDeleteFilePropertyDialog(
    filename: String,
    onDismissRequest: () -> Unit,
    onConfirmed: () -> Unit,
) {
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


@Composable
fun EditCaptionDialog(
    fileProperty: FileProperty,
    onDismiss: () -> Unit,
    onSave: (FileProperty.Id, newCaption: String) -> Unit
) {

    var captionText: String by remember {
        mutableStateOf(fileProperty.comment ?: "")
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    stringResource(R.string.edit_caption),
                    fontSize = 24.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = captionText,
                    placeholder = {
                        Text(stringResource(R.string.input_caption))
                    },
                    onValueChange = { text ->
                    captionText = text
                    }
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        onSave.invoke(fileProperty.id, captionText)
                    }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}