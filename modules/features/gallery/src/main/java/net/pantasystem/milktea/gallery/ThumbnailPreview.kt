package net.pantasystem.milktea.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.model.drive.FileProperty

@Composable
fun ThumbnailPreview(
    file: FileProperty,
    modifier: Modifier = Modifier,
    visibleFileIds: Set<FileProperty.Id>,
    onClick: () -> Unit,
) {

    val isVisible = visibleFileIds.contains(file.id) || !file.isSensitive
    Box(
        modifier = modifier.clickable(onClick = onClick).fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if(!isVisible) {
            Text(stringResource(R.string.sensitive_content))
        }
        

        if(isVisible) {
            Image(
                painter = rememberAsyncImagePainter(
                    file.thumbnailUrl ?: "",
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize(),
            )
        }

        if(file.type.startsWith("video")) {
            Image(
                Icons.Default.PlayCircleOutline,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier.fillMaxSize(),
            )
        }


    }

}
