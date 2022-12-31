package net.pantasystem.milktea.common_compose

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile


@Composable
fun HorizontalFilePreviewList(
    files: List<AppFile>,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onAction: (FilePreviewActionType) -> Unit,
    modifier: Modifier = Modifier) {
    LazyRow(
        modifier
    ) {
        items(count = files.size) { index ->
            FilePreview(
                file = files[index],
                repository = repository,
                dataSource = dataSource,
                onAction = onAction
            )
        }
    }
}

sealed interface FilePreviewSource {
    val file: AppFile
    data class Local(override val file: AppFile.Local) : FilePreviewSource
    data class Remote(override val file: AppFile.Remote, val fileProperty: FileProperty) :
        FilePreviewSource
}

sealed interface FilePreviewActionType {
    val target: FilePreviewSource
    data class Show(override val target: FilePreviewSource) : FilePreviewActionType
    data class Detach(override val target: FilePreviewSource) : FilePreviewActionType
    data class ToggleSensitive(override val target: FilePreviewSource) : FilePreviewActionType
}

@Composable
fun FilePreview(
    file: AppFile,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onAction: (FilePreviewActionType) -> Unit,
) {
    var dropDownTarget: FilePreviewSource? by remember {
        mutableStateOf(null)
    }
    Column {
        when(file) {
            is AppFile.Local -> {
                LocalFilePreview(
                    file = file,
                    onClick = {
                        dropDownTarget = FilePreviewSource.Local(it)
                    }
                )
            }
            is AppFile.Remote -> {
                RemoteFilePreview(
                    file = file,
                    repository = repository,
                    dataSource = dataSource,
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
    onClick: (AppFile.Local) -> Unit
) {
    val uri = Uri.parse(file.path)
    Box (
        modifier = Modifier
            .size(100.dp)
            .padding(horizontal = 2.dp)
            .clickable {
                onClick(file)
            }
    ){
        Box (contentAlignment = Alignment.TopEnd) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if(file.isSensitive) {
                SensitiveIcon()
            }
        }

    }
}

@Composable
fun RemoteFilePreview(
    file: AppFile.Remote, repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onClick: (FileProperty) -> Unit
) {
    var filePropertyState: ResultState<FileProperty> by remember {
        mutableStateOf(ResultState.Loading(content = StateContent.NotExist()))
    }
    val fileProperty = dataSource.observe(file.id)
        .collectAsState(null)


    LaunchedEffect(key1 = file.id) {
        runCancellableCatching {
            repository.find(file.id)
        }.onSuccess {
            filePropertyState = ResultState.Fixed(
                StateContent.Exist(it)
            )
        }.onFailure {
            filePropertyState = ResultState.Error(
                filePropertyState.content,
                throwable = it
            )
        }
    }


    Box (
        modifier = Modifier
            .size(100.dp)
            .padding(horizontal = 2.dp)
            .clickable {
                if (fileProperty.value != null) {
                    onClick(fileProperty.value!!)
                }
            }
    ){
        when(filePropertyState.content) {
            is StateContent.Exist -> {
                val content = (filePropertyState.content as StateContent.Exist).rawContent
                Box (contentAlignment = Alignment.TopEnd){
                    Image(
                        painter = rememberAsyncImagePainter(
                            fileProperty.value?.thumbnailUrl
                                ?: content.thumbnailUrl
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    if(fileProperty.value?.isSensitive?: content.isSensitive){
                        SensitiveIcon()
                    }
                }
            }
            is StateContent.NotExist -> {
                CircularProgressIndicator()
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
    onDismissRequest: ()->Unit,
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
                if(isSensitive)
                    Icons.Filled.Image
                else
                    Icons.Filled.HideImage,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                if(isSensitive) stringResource(id = R.string.undo_nsfw) else stringResource(id = R.string.mark_as_nsfw)
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