package jp.panta.misskeyandroidclient.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.rememberImagePainter
import jp.panta.misskeyandroidclient.R
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
            Text(stringResource(R.string.nsfw_message))
        }
        

        if(isVisible) {
            Image(
                painter = rememberImagePainter(
                    file.thumbnailUrl ?: "",
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize(),
            )
        }

        if(file.type.startsWith("video")) {
            Image(
                painter = painterResource(id = R.drawable.ic_play_circle_outline_black_24dp),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier.fillMaxSize(),
            )
        }


    }

}
