package jp.panta.misskeyandroidclient.ui.components

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import coil.compose.rememberAsyncImagePainter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.common.State
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.components.SensitiveIcon
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

sealed interface FilePreviewTarget {
    val file: AppFile
    data class Local(override val file: AppFile.Local) : FilePreviewTarget
    data class Remote(override val file: AppFile.Remote, val fileProperty: FileProperty) : FilePreviewTarget
}

sealed interface FilePreviewActionType {
    val target: FilePreviewTarget
    data class Show(override val target: FilePreviewTarget) : FilePreviewActionType
    data class Detach(override val target: FilePreviewTarget) : FilePreviewActionType
    data class ToggleSensitive(override val target: FilePreviewTarget) : FilePreviewActionType
}

@Composable
fun FilePreview(
    file: AppFile,
    repository: DriveFileRepository,
    dataSource: FilePropertyDataSource,
    onAction: (FilePreviewActionType) -> Unit,
) {
    var dropDownTarget: FilePreviewTarget? by remember {
        mutableStateOf(null)
    }
    Column {
        when(file) {
            is AppFile.Local -> {
                LocalFilePreview(
                    file = file,
                    onClick = {
                        dropDownTarget = FilePreviewTarget.Local(it)
                    }
                )
            }
            is AppFile.Remote -> {
                RemoteFilePreview(
                    file = file,
                    repository = repository,
                    dataSource = dataSource,
                    onClick = {
                        dropDownTarget = FilePreviewTarget.Remote(
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
                        (target is FilePreviewTarget.Local && target.file.isSensitive)
                        || (target is FilePreviewTarget.Remote && target.fileProperty.isSensitive)
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
    var filePropertyState: State<FileProperty> by remember {
        mutableStateOf(State.Loading(content = StateContent.NotExist()))
    }
    val fileProperty = dataSource.observe(file.id)
        .asLiveData()
        .observeAsState()


    LaunchedEffect(key1 = file.id) {
        runCatching {
            repository.find(file.id)
        }.onSuccess {
            filePropertyState = State.Fixed(
                StateContent.Exist(it)
            )
        }.onFailure {
            filePropertyState = State.Error(
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