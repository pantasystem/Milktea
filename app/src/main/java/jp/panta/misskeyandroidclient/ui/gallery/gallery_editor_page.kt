package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryEditorViewModel
import net.pantasystem.milktea.model.file.AppFile

sealed interface GalleryEditorPageAction {
    object NavigateUp : GalleryEditorPageAction
    data class NavigateToMediaPreview(val appFile: AppFile) : GalleryEditorPageAction
    object PickDriveFile : GalleryEditorPageAction
    object PickLocalFile : GalleryEditorPageAction
    object OnSave : GalleryEditorPageAction
}

@Composable
fun GalleryEditorPage(galleryEditorViewModel: GalleryEditorViewModel, onAction: (GalleryEditorPageAction) -> Unit) {

    val state by galleryEditorViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_gallery))
                },
                navigationIcon = {
                    IconButton(onClick = { onAction.invoke(GalleryEditorPageAction.NavigateUp) }) {
                        Icons.Default.ArrowBack
                    }
                }
            )
        }
    ) {

        Column(Modifier.padding(it).fillMaxSize()) {
            LazyColumn(
                Modifier.padding(16.dp)
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                item {
                    if (state.pickedImages.isNotEmpty()) {
                        PickedImagePreview(
                            viewModel = galleryEditorViewModel,
                            repository = galleryEditorViewModel.driveFileRepository,
                            dataSource = galleryEditorViewModel.filePropertyDataSource,
                            onShow = { file ->
                                onAction.invoke(GalleryEditorPageAction.NavigateToMediaPreview(file.file))
                            }
                        )
                    }
                    Text(stringResource(id = R.string.pick_image))

                    Button(onClick = { onAction.invoke(GalleryEditorPageAction.PickDriveFile) }) {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                        Text(modifier = Modifier.fillMaxWidth(), text = stringResource(id = R.string.pick_image_from_device))
                    }
                    Button(onClick = { onAction.invoke(GalleryEditorPageAction.PickLocalFile) }) {
                        Icon(Icons.Default.PhotoAlbum, contentDescription = null)
                        Text(modifier = Modifier.fillMaxWidth(), text = stringResource(id = R.string.pick_image_from_drive))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.title,
                        label = {
                            Text(stringResource(id = R.string.title))
                        },
                        onValueChange = { title ->
                            galleryEditorViewModel.setTitle(title)
                        },
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.description ?: "",
                        label = {
                            Text(stringResource(id = R.string.description))
                        },
                        onValueChange = { text ->
                            galleryEditorViewModel.setDescription(text)
                        }
                    )
                }

            }

            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp ,end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onAction.invoke(GalleryEditorPageAction.OnSave) }) {
                    Text(stringResource(id = R.string.save))
                }
            }


        }


    }
}

