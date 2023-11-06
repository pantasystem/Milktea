package net.pantasystem.milktea.channel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.user.User

sealed interface ChannelCardAction {
    val channel: Channel

    data class OnClick(override val channel: Channel) : ChannelCardAction
    data class OnFollowButtonClicked(override val channel: Channel) : ChannelCardAction
    data class OnUnFollowButtonClicked(override val channel: Channel) : ChannelCardAction
    data class OnToggleTabButtonClicked(override val channel: Channel) : ChannelCardAction
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Stable
fun ChannelCard(
    channel: Channel,
    isPaged: Boolean,
    onAction: (ChannelCardAction) -> Unit = {},
) {
    Card(elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        onClick = {
            onAction.invoke(ChannelCardAction.OnClick(channel))
        }) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            ChannelCardHeader(channel = channel)

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
                        channel.name, fontSize = 18.sp
                    )
                    if (!channel.description.isNullOrBlank()) {
                        Text(channel.description ?: "", maxLines = 3)
                    }
                }
                ChannelCardActionButtons(channel = channel, isPaged = isPaged, onAction = onAction)
            }
        }
    }
}

@Composable
@Stable
private fun AddToTabButton(isPaged: Boolean, onPressed: () -> Unit) {
    IconButton(onClick = onPressed) {
        if (isPaged) {
            Icon(
                painter = painterResource(R.drawable.ic_remove_to_tab_24px),
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_add_to_tab_24px),
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        }

    }
}

@Composable
@Stable
private fun ChannelCardHeader(channel: Channel) {
    val (r, g, b) = channel.rgpFromName
    Box {
        Image(
            painter = rememberAsyncImagePainter(channel.bannerUrl),
            contentDescription = "header",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .background(Color(r, g, b))
                .fillMaxWidth()
                .height(150.dp)
        )

        ChannelCardAggregateLabel(
            channel = channel, modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
@Stable
private fun ChannelCardActionButtons(
    channel: Channel, isPaged: Boolean, onAction: (ChannelCardAction) -> Unit
) {
    Row {
        AddToTabButton(isPaged = isPaged, onPressed = {
            onAction.invoke(
                ChannelCardAction.OnToggleTabButtonClicked(channel)
            )
        })
        if (channel.isFollowing != null) {
            ToggleFollowButton(isFollowing = channel.isFollowing!!, onChanged = { followed ->
                if (followed) {
                    onAction.invoke(
                        ChannelCardAction.OnFollowButtonClicked(
                            channel
                        )
                    )
                } else {
                    onAction.invoke(
                        ChannelCardAction.OnUnFollowButtonClicked(
                            channel
                        )
                    )
                }
            })
        }
    }
}

@Composable
private fun ChannelCardAggregateLabel(modifier: Modifier = Modifier, channel: Channel) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.75f),
        contentColor = Color.White,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle, contentDescription = "users count"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(stringResource(id = R.string.channel_n_people, channel.usersCount))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit, contentDescription = "posts count"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(stringResource(id = R.string.channel_n_posts, channel.notesCount))
            }
        }
    }
}

@Composable
private fun ToggleFollowButton(isFollowing: Boolean, onChanged: (Boolean) -> Unit) {
    if (isFollowing) {
        Button(
            onClick = {
                onChanged.invoke(false)
            },
            shape = RoundedCornerShape(32.dp)
        ) {
            Text(stringResource(id = R.string.unfollow))
        }
    } else {
        OutlinedButton(
            onClick = {
                onChanged.invoke(true)
            }, shape = RoundedCornerShape(32.dp)
        ) {
            Text(stringResource(id = R.string.follow))
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
                    hasUnreadNote = true,
                    allowRenoteToExternal = true,
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
                    hasUnreadNote = false,
                    allowRenoteToExternal = true,
                ), isPaged = false
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
                    hasUnreadNote = false,
                    allowRenoteToExternal = true,
                ), isPaged = false
            )
        }
    }
}