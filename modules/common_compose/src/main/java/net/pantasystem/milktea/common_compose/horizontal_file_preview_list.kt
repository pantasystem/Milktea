package net.pantasystem.milktea.common_compose

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource


@Composable
fun HorizontalFilePreviewList(
    modifier: Modifier = Modifier,
    files: List<FilePreviewSource>,
    allowMaxFileSize: Long? = null,
    onAction: (FilePreviewActionType) -> Unit,
) {
    LazyRow(
        modifier
    ) {
        items(count = files.size) { index ->
            FilePreview(
                file = files[index],
                allowMaxFileSize = allowMaxFileSize,
                onAction = onAction
            )
        }
    }
}


sealed interface FilePreviewActionType {
    val target: FilePreviewSource

    data class Show(override val target: FilePreviewSource) : FilePreviewActionType
    data class Detach(override val target: FilePreviewSource) : FilePreviewActionType
    data class ToggleSensitive(override val target: FilePreviewSource) : FilePreviewActionType
}

@Composable
fun FilePreview(
    file: FilePreviewSource,
    allowMaxFileSize: Long?,
    onAction: (FilePreviewActionType) -> Unit,
) {
    var dropDownTarget: FilePreviewSource? by remember {
        mutableStateOf(null)
    }
    Column {
        when (file) {
            is FilePreviewSource.Local -> {
                LocalFilePreview(
                    file = file.file,
                    allowMaxFileSize = allowMaxFileSize,
                    onClick = {
                        dropDownTarget = FilePreviewSource.Local(it)
                    }
                )
            }
            is FilePreviewSource.Remote -> {
                RemoteFilePreview(
                    fileProperty = file.fileProperty,
                    onClick = {
                        dropDownTarget = FilePreviewSource.Remote(
                            AppFile.Remote(it.id),
                            it
                        )
                    }
                )
            }
        }
        val target = dropDownTarget
        FilePreviewActionDropDown(
            isSensitive = target != null
                    && (
                    (target is FilePreviewSource.Local && target.file.isSensitive)
                            || (target is FilePreviewSource.Remote && target.fileProperty.isSensitive)
                    ),
            expanded = dropDownTarget != null,
            onToggleSensitive = {
                onAction(
                    FilePreviewActionType.ToggleSensitive(
                        dropDownTarget!!
                    )
                )
            },
            onDetach = {
                onAction(
                    FilePreviewActionType.Detach(
                        dropDownTarget!!
                    )
                )
            },
            onShow = {
                onAction(
                    FilePreviewActionType.Show(
                        dropDownTarget!!
                    )
                )
            },
            onDismissRequest = {
                dropDownTarget = null
            }
        )
    }

}

@Composable
fun LocalFilePreview(
    file: AppFile.Local,
    allowMaxFileSize: Long?,
    onClick: (AppFile.Local) -> Unit
) {
    val uri = Uri.parse(file.path)
    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(horizontal = 2.dp)
            .clickable {
                onClick(file)
            }
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (file.isSensitive) {
                SensitiveIcon(
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            if (allowMaxFileSize != null && file.fileSize != null) {
                if (allowMaxFileSize < (file.fileSize ?: 0)) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                }
            }


        }

    }
}

@Composable
fun RemoteFilePreview(
    fileProperty: FileProperty,
    onClick: (FileProperty) -> Unit
) {


    Box(
        modifier = Modifier
            .size(100.dp)
            .padding(horizontal = 2.dp)
            .clickable {
                onClick(fileProperty)
            }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Image(
                painter = rememberAsyncImagePainter(
                    fileProperty.thumbnailUrl
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (fileProperty.isSensitive) {
                SensitiveIcon()
            }
        }
    }

}


@Composable
fun FilePreviewActionDropDown(
    isSensitive: Boolean,
    onToggleSensitive: (Boolean) -> Unit,
    onDetach: () -> Unit,
    onShow: () -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.wrapContentWidth(),
    ) {
        DropdownMenuItem(
            onClick = {
                onDetach()
                onDismissRequest()
            }
        ) {
            Icon(
                Icons.Filled.RemoveCircle,
                contentDescription = stringResource(id = R.string.remove_attachment),
                modifier = Modifier.size(24.dp)
            )
            Text(stringResource(id = R.string.remove_attachment))
        }
        DropdownMenuItem(
            onClick = {
                onToggleSensitive(!isSensitive)
                onDismissRequest()
            }
        ) {

            Icon(
                if (isSensitive)
                    Icons.Filled.Image
                else
                    Icons.Filled.HideImage,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                if (isSensitive) stringResource(id = R.string.undo_nsfw) else stringResource(id = R.string.mark_as_nsfw)
            )
        }
        DropdownMenuItem(
            onClick = {
                onShow()
                onDismissRequest()
            }
        ) {
            Icon(
                Icons.Filled.Details,
                contentDescription = stringResource(id = R.string.show),
                modifier = Modifier.size(24.dp)
            )
            Text(
                stringResource(id = R.string.show)
            )
        }

    }
}