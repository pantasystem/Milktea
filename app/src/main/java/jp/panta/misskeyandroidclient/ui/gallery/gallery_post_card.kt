@file:OptIn(ExperimentalPagerApi::class)

package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.components.ThumbnailPreview
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryPostUiState
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User


sealed interface GalleryPostCardAction {
    object OnAvatarIconClicked : GalleryPostCardAction
    data class OnThumbnailClicked(val fileProperty: FileProperty) : GalleryPostCardAction
    data class OnFavoriteButtonClicked(val value: Boolean) : GalleryPostCardAction
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryPostCard(
    galleryState: GalleryPostUiState,
    onAction: (GalleryPostCardAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = galleryState.files.size)


    Card(
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(
                        galleryState.user.avatarUrl,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(onClick = { onAction.invoke(GalleryPostCardAction.OnAvatarIconClicked) })
                        .padding(end = 4.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        galleryState.user.getDisplayName(),
                        maxLines = 1,
                    )
                    Text(
                        galleryState.user.getDisplayUserName(),
                        maxLines = 1,
                    )
                }

            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) { page ->
                ThumbnailPreview(file = galleryState.files[page]) {
                    onAction.invoke(GalleryPostCardAction.OnThumbnailClicked(galleryState.files[page]))
                }
            }
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                )
            }
            Row(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        galleryState.galleryPost.title,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp
                    )
                    if (galleryState.galleryPost.description != null) {
                        Text(galleryState.galleryPost.description ?: "")
                    }
                }

                if (galleryState.galleryPost is GalleryPost.Authenticated) {
                    GalleryFavoriteButton(
                        checked = galleryState.galleryPost.isLiked,
                        onChanged = {
                            onAction.invoke(GalleryPostCardAction.OnFavoriteButtonClicked(it))
                        }
                    )
                }
            }

        }
    }
}

@Composable
private fun GalleryFavoriteButton(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    IconButton(onClick = { onChanged.invoke(!checked) }, modifier = modifier) {
        if (checked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_red_favorite_24),
                contentDescription = null,
                tint = Color(red = 0xFF, green = 0x65, blue = 0x5B)
            )
        } else {
            Icon(
                painterResource(id = R.drawable.ic_baseline_favorite_border_24),
                contentDescription = null,
                tint = Color(red = 0xFF, green = 0x65, blue = 0x5B)
            )
        }

    }
}

@Preview
@Composable
fun PreviewGalleryPostCard() {
    GalleryPostCard(
        galleryState = GalleryPostUiState(
            galleryPost = GalleryPost.Authenticated(
                id = GalleryPost.Id(0L, "id"),
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now(),
                description = "hogehoge",
                fileIds = listOf(),
                isLiked = true,
                isSensitive = false,
                likedCount = 10,
                tags = emptyList(),
                title = "ギャラリーかも",
                userId = User.Id(0L, "userId")
            ),
            files = listOf(
                FileProperty(
                    FileProperty.Id(0L, "1"),
                    createdAt = Clock.System.now(),
                    md5 = "",
                    thumbnailUrl = "",
                    name = "test",
                    size = 500,
                    type = "image/png",
                    url = ""
                ),
                FileProperty(
                    FileProperty.Id(0L, "1"),
                    createdAt = Clock.System.now(),
                    md5 = "",
                    thumbnailUrl = "",
                    name = "test",
                    size = 500,
                    type = "image/png",
                    url = ""
                ),
                FileProperty(
                    FileProperty.Id(0L, "1"),
                    createdAt = Clock.System.now(),
                    md5 = "",
                    thumbnailUrl = "",
                    name = "test",
                    size = 500,
                    type = "image/png",
                    url = ""
                ),
                FileProperty(
                    FileProperty.Id(0L, "1"),
                    createdAt = Clock.System.now(),
                    md5 = "",
                    thumbnailUrl = "",
                    name = "test",
                    size = 500,
                    type = "image/png",
                    url = ""
                )
            ),
            currentIndex = 0,
            isFavoriteSending = false,
            user = UserDTO(
                id = "test",
                name = "testname",
                userName = "testtest"
            ).toUser(0L)
        ),
        onAction = {})
}