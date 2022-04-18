package jp.panta.misskeyandroidclient.ui.channel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.user.User
import kotlinx.datetime.Clock

sealed interface ChannelCardAction {
    val channel: Channel

    data class OnClick(override val channel: Channel) : ChannelCardAction
    data class OnFollowButtonClicked(override val channel: Channel) : ChannelCardAction
    data class OnUnFollowButtonClicked(override val channel: Channel) : ChannelCardAction
    data class OnToggleTabButtonClicked(override val channel: Channel) : ChannelCardAction
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChannelCard(
    channel: Channel,
    isPaged: Boolean,
    onAction: (ChannelCardAction) -> Unit = {},
) {
    val (r, g, b) = channel.rgpFromName
    Card(
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            onAction.invoke(ChannelCardAction.OnClick(channel))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Box {
                Image(
                    painter = rememberImagePainter(channel.bannerUrl),
                    contentDescription = "header",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .background(Color( r, g, b))
                        .height(150.dp)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "users count"
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(stringResource(id = R.string.n_people, channel.usersCount))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "posts count"
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(stringResource(id = R.string.n_posts, channel.notesCount))
                        }
                    }
                }

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f, true)
                ) {
                    Text(
                        channel.name,
                        fontSize = 18.sp
                    )
                    if (!channel.description.isNullOrBlank()) {
                        Text(channel.description ?: "", maxLines = 3)
                    }
                }
                Row {
                    IconButton(onClick = {
                        onAction.invoke(ChannelCardAction.OnToggleTabButtonClicked(channel))
                    }) {
                        if (isPaged) {
                            Icon(
                                imageVector = Icons.Default.BookmarkRemove,
                                contentDescription = "add to tab",
                                tint = MaterialTheme.colors.secondary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "add to tab",
                                tint = MaterialTheme.colors.secondary
                            )
                        }

                    }
                    if (channel.isFollowing != null) {
                        if (channel.isFollowing!!) {
                            Button(onClick = {
                                onAction.invoke(
                                    ChannelCardAction.OnUnFollowButtonClicked(
                                        channel
                                    )
                                )
                            }) {
                                Text(stringResource(id = R.string.unfollow))
                            }
                        } else {
                            OutlinedButton(onClick = {
                                onAction.invoke(
                                    ChannelCardAction.OnFollowButtonClicked(
                                        channel
                                    )
                                )
                            }) {
                                Text(stringResource(id = R.string.follow))
                            }
                        }
                    }

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewChannelCard() {
    LazyColumn {
        item {
            ChannelCard(
                Channel(
                    id = Channel.Id(0, "channelId"),
                    bannerUrl = "https://s3.arkjp.net/misskey/00edb5ca-2e15-45e9-b7cb-a2ee6c7c7e1e.jpg",
                    createdAt = Clock.System.now(),
                    description = "消えたので作った",
                    name = "はるのんしすてむどっとこむ",
                    lastNotedAt = Clock.System.now(),
                    notesCount = 10,
                    userId = User.Id(0, "userId"),
                    usersCount = 4,
                    isFollowing = true,
                    hasUnreadNote = true
                ),
                isPaged = true,
            )
        }
        item {
            ChannelCard(
                Channel(
                    id = Channel.Id(0, "channelId"),
                    bannerUrl = "https://s3.arkjp.net/misskey/00edb5ca-2e15-45e9-b7cb-a2ee6c7c7e1e.jpg",
                    createdAt = Clock.System.now(),
                    description = "説明説明説明説明説明説明",
                    name = "パン太は人間だよ",
                    lastNotedAt = Clock.System.now(),
                    notesCount = 10,
                    userId = User.Id(0, "userId"),
                    usersCount = 4,
                    isFollowing = false,
                    hasUnreadNote = false
                ),
                isPaged = false
            )
        }
        item {
            ChannelCard(
                Channel(
                    id = Channel.Id(0, "channelId"),
                    bannerUrl = "https://s3.arkjp.net/misskey/00edb5ca-2e15-45e9-b7cb-a2ee6c7c7e1e.jpg",
                    createdAt = Clock.System.now(),
                    description = "説明説明説明説明説明説明",
                    name = "a",
                    lastNotedAt = Clock.System.now(),
                    notesCount = 10,
                    userId = User.Id(0, "userId"),
                    usersCount = 4,
                    isFollowing = false,
                    hasUnreadNote = false
                ),
                isPaged = false
            )
        }
    }
}