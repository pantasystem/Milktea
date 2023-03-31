package net.pantasystem.milktea.gallery

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.common_compose.SwitchTile
import net.pantasystem.milktea.gallery.viewmodel.GalleryEditorViewModel
import net.pantasystem.milktea.model.file.FilePreviewSource

sealed interface GalleryEditorPageAction {
    object NavigateUp : GalleryEditorPageAction
    data class NavigateToMediaPreview(val file: FilePreviewSource) : GalleryEditorPageAction
    object PickDriveFile : GalleryEditorPageAction
    object PickLocalFile : GalleryEditorPageAction
    object OnSave : GalleryEditorPageAction
}

@Composable
fun GalleryEditorPage(
    galleryEditorViewModel: GalleryEditorViewModel,
    onAction: (GalleryEditorPageAction) -> Unit
) {

    val state by galleryEditorViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.create_gallery))
                },
                navigationIcon = {
                    IconButton(onClick = { onAction.invoke(GalleryEditorPageAction.NavigateUp) }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {

        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn(
                Modifier
                    .padding(16.dp)
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                item {
                    Text(stringResource(id = R.string.gallery_pick_image))

                    if (state.pickedImages.isNotEmpty()) {
                        PickedImagePreview(
                            viewModel = galleryEditorViewModel,
                            onShow = { file ->
                                onAction.invoke(GalleryEditorPageAction.NavigateToMediaPreview(file))
                            }
                        )
                    }

                    Button(onClick = { onAction.invoke(GalleryEditorPageAction.PickDriveFile) }) {
                        Icon(Icons.Default.Cloud, contentDescription = null)
                        Text(
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.gallery_pick_image_from_device)
                        )
                    }
                    Button(onClick = { onAction.invoke(GalleryEditorPageAction.PickLocalFile) }) {
                        Icon(Icons.Default.PhotoAlbum, contentDescription = null)
                        Text(
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.gallery_pick_image_from_drive)
                        )
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

                    Spacer(modifier = Modifier.height(8.dp))

                    SwitchTile(
                        checked = state.isSensitive,
                        onChanged = {
                            galleryEditorViewModel.toggleSensitive()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.sensitive))
                    }
                }

            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = { onAction.invoke(GalleryEditorPageAction.OnSave) }) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    }
}

