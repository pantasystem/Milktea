package jp.panta.misskeyandroidclient.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent


@Composable
fun HorizontalFilePreviewList(files: List<AppFile>, repository: DriveFileRepository, modifier: Modifier = Modifier) {
    LazyRow(
        modifier
    ) {
        items(count = files.size) { index ->
            FilePreview(file = files[index], repository = repository)
        }
    }
}

@Composable
fun FilePreview(file: AppFile, repository: DriveFileRepository) {
    when(file) {
        is AppFile.Local -> {
            LocalFilePreview(file = file)
        }
        is AppFile.Remote -> {
            RemoteFilePreview(file = file, repository = repository)
        }
    }
}

@Composable
fun LocalFilePreview(file: AppFile.Local) {
    val uri = Uri.parse(file.path)
    Box (
        modifier = Modifier
            .size(100.dp)
            .padding(horizontal = 2.dp)
    ){
        Image(
            painter = rememberImagePainter(uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun RemoteFilePreview(file: AppFile.Remote, repository: DriveFileRepository) {
    var filePropertyState: State<FileProperty>  by remember {
        mutableStateOf(State.Loading(content = StateContent.NotExist()))
    }
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
    ){
        when(filePropertyState.content) {
            is StateContent.Exist -> {
                val content = (filePropertyState.content as StateContent.Exist).rawContent
                Image(
                    painter = rememberImagePainter(content.thumbnailUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            is StateContent.NotExist -> {
                CircularProgressIndicator()
            }
        }
    }

}

