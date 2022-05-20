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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
    val galleryPost: GalleryPost

    data class OnAvatarIconClicked(override val galleryPost: GalleryPost) : GalleryPostCardAction
    data class OnThumbnailClicked(
        override val galleryPost: GalleryPost,
        val fileProperty: FileProperty,
        val files: List<FileProperty>,
        val index: Int,
    ) : GalleryPostCardAction

    data class OnFavoriteButtonClicked(override val galleryPost: GalleryPost, val value: Boolean) :
        GalleryPostCardAction
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryPostCard(
    galleryState: GalleryPostUiState,
    visibleFileIds: Set<FileProperty.Id>,
    onAction: (GalleryPostCardAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = galleryState.files.size)


    Card(
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        galleryState.user.avatarUrl,
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(onClick = {
                            onAction.invoke(
                                GalleryPostCardAction.OnAvatarIconClicked(
                                    galleryState.galleryPost
                                )
                            )
                        })
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
            Spacer(Modifier.height(4.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) { page ->
                ThumbnailPreview(file = galleryState.files[page], visibleFileIds = visibleFileIds) {
                    onAction.invoke(
                        GalleryPostCardAction.OnThumbnailClicked(
                            galleryState.galleryPost,
                            galleryState.files[page],
                            galleryState.files,
                            page,
                        )
                    )
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        galleryState.galleryPost.title,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 20.sp
                    )
                    if (!galleryState.galleryPost.description.isNullOrBlank()) {
                        Text(galleryState.galleryPost.description ?: "")
                    }
                }

                if (galleryState.galleryPost is GalleryPost.Authenticated) {
                    GalleryFavoriteButton(
                        checked = galleryState.galleryPost.isLiked,
                        enabled = !galleryState.isFavoriteSending,
                        onChanged = {
                            onAction.invoke(
                                GalleryPostCardAction.OnFavoriteButtonClicked(
                                    galleryState.galleryPost,
                                    it
                                )
                            )
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
    enabled: Boolean,
    onChanged: (Boolean) -> Unit
) {
    IconButton(
        onClick = { onChanged.invoke(!checked) },
        modifier = modifier,
        enabled = enabled
    ) {
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
                name = "harunon",
                userName = "harunonsysytem"
            ).toUser(0L)
        ),
        onAction = {}, visibleFileIds = emptySet()
    )
}