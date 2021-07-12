package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.pager.*
import jp.panta.misskeyandroidclient.api.v12_75_0.GalleryPost
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.ui.components.ThumbnailPreview
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostState
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import jp.panta.misskeyandroidclient.R



@ExperimentalPagerApi
@Composable
fun GalleryPostCard(
    galleryState: GalleryPostState,
    onAvatarIconClicked: ()->Unit,
    onThumbnailClicked: (FileProperty)->Unit
) {
    val pagerState = rememberPagerState(pageCount = galleryState.files.size)

    var isExpanded: Boolean by remember {
        mutableStateOf(false)
    }

    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberGlidePainter(
                        request = galleryState.user.avatarUrl,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarIconClicked)
                        .padding(end = 4.dp),
                )

                Text(
                    galleryState.user.getDisplayName(),
                    maxLines = 1,
                )
            }
            HorizontalPager(state = pagerState) { page ->
                ThumbnailPreview(file = galleryState.files[page]) {
                    onThumbnailClicked.invoke(galleryState.files[page])
                }
            }
            HorizontalPagerIndicator(pagerState = pagerState)
            Row {
                Text(galleryState.galleryPost.title)
                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                    }
                ) {
                    Icon(
                        painter = painterResource(id = if(isExpanded) R.drawable.ic_expand_less_black_24dp else R.drawable.ic_expand_more_black_24dp),
                        contentDescription = null
                    )
                }
            }
        }
    }
}