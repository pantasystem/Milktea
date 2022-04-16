package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.data.model.drive.FileProperty

@Composable
fun FileActionDropdownMenu(
    property: FileProperty,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onNsfwMenuItemClicked: () -> Unit,
    onDeleteMenuItemClicked: ()-> Unit,
) {


    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentWidth(),
    ) {
        DropdownMenuItem(
            onClick = onNsfwMenuItemClicked
        ) {
            if(property.isSensitive) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_image_24),
                    contentDescription = stringResource(R.string.undo_nsfw),
                    modifier = Modifier.size(24.dp)

                )
                Text(stringResource(R.string.undo_nsfw))
            }else{
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_baseline_hide_image_24),
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