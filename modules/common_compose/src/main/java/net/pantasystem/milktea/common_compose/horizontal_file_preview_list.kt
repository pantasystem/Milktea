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
import androidx.compose.ui.graphics.Color
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
    isMisskey: Boolean = true,
    files: List<FilePreviewSource>,
    allowMaxFileSize: Long? = null,
    onShow: (FilePreviewSource) -> Unit,
    onDetach: (FilePreviewSource) -> Unit,
    onToggleSensitive: (FilePreviewSource) -> Unit,
    onEditFileCaption: ((FilePreviewSource) -> Unit)? = null,
    onEditFileName: ((FilePreviewSource) -> Unit)? = null,
) {
    LazyRow(
        modifier
    ) {
        items(count = files.size) { index ->
            FilePreview(
                isMisskey = isMisskey,
                file = files[index],
                allowMaxFileSize = allowMaxFileSize,
                onShow = onShow,
                onDetach = onDetach,
                onToggleSensitive = onToggleSensitive,
                onEditFileName = onEditFileName,
                onEditFileCaption = onEditFileCaption,
            )
        }
    }
}



@Composable
fun FilePreview(
    isMisskey: Boolean = true,
    file: FilePreviewSource,
    allowMaxFileSize: Long?,
    onShow: (FilePreviewSource) -> Unit,
    onDetach: (FilePreviewSource) -> Unit,
    onToggleSensitive: (FilePreviewSource) -> Unit,
    onEditFileCaption: ((FilePreviewSource) -> Unit)? = null,
    onEditFileName: ((FilePreviewSource) -> Unit)? = null,
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
            isMisskey = isMisskey,
            isSensitive = target != null
                    && (
                    (target is FilePreviewSource.Local && target.file.isSensitive)
                            || (target is FilePreviewSource.Remote && target.fileProperty.isSensitive)
                    ),
            expanded = dropDownTarget != null,
            onToggleSensitive = {
                onToggleSensitive(dropDownTarget!!)
            },
            onDetach = {
                onDetach(dropDownTarget!!)

            },
            onShow = {
                onShow(dropDownTarget!!)

            },
            onDismissRequest = {
                dropDownTarget = null
            },
            onEditFileCaption = onEditFileCaption?.let{
                {
                    it(dropDownTarget!!)
                }

            },
            onEditFileName = onEditFileName?.let {
                {
                    it(dropDownTarget!!)
                }
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
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.padding(2.dp)
                    )
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
    isMisskey: Boolean = true,
    isSensitive: Boolean,
    onToggleSensitive: (Boolean) -> Unit,
    onDetach: () -> Unit,
    onShow: () -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditFileName: (() -> Unit)?,
    onEditFileCaption: (() -> Unit)?,
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

        if (isMisskey) {
            if (onEditFileName != null) {

                DropdownMenuItem(onClick = {
                    onEditFileName()
                    onDismissRequest()
                }) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource(id = R.string.edit_file_name), modifier = Modifier.size(24.dp))
                    Text(stringResource(R.string.edit_file_name))
                }
            }
        }

        if (onEditFileCaption != null) {
            DropdownMenuItem(
                onClick = {
                    onEditFileCaption()
                    onDismissRequest()
                }
            ) {
                Icon(Icons.Filled.Comment, contentDescription = stringResource(id = R.string.edit_file_caption))
                Text(stringResource(id = R.string.edit_file_caption))
            }
        }
    }
}