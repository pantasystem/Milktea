@file:OptIn(ExperimentalPagerApi::class)

package net.pantasystem.milktea.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.AvatarIcon
import net.pantasystem.milktea.common_compose.FavoriteButton
import net.pantasystem.milktea.gallery.viewmodel.GalleryPostUiState
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.gallery.GalleryPost
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make


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
                AvatarIcon(
                    url = galleryState.user.avatarUrl,
                    size = 50.dp,
                    onAvatarClick = {
                        onAction.invoke(
                            GalleryPostCardAction.OnAvatarIconClicked(
                                galleryState.galleryPost
                            )
                        )

                    },
                    modifier = Modifier.padding(4.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        galleryState.user.displayName,
                        maxLines = 1,
                    )
                    Text(
                        galleryState.user.displayUserName,
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
                    FavoriteButton(isFavorite = galleryState.galleryPost.isLiked, onClick = {
                        onAction.invoke(
                            GalleryPostCardAction.OnFavoriteButtonClicked(
                                galleryState.galleryPost,
                                !galleryState.galleryPost.isLiked,
                            )
                        )
                    })
                }
            }

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
            user = User.Simple.make(
                id = User.Id(0, "test"),
                name = "harunon",
                userName = "harunonsysytem"
            )
        ),
        onAction = {}, visibleFileIds = emptySet()
    )
}