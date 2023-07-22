package net.pantasystem.milktea.messaging

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.AvatarIcon
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.common_compose.getSimpleElapsedTime
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.make
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make

@Composable
@Stable
fun SelfMessageBubble(
    message: Message,
    accountHost: String?,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Spacer(Modifier.width(48.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp),
                color = MaterialTheme.colors.primary,
                elevation = 4.dp
            ) {
                Column(
                    Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (message.text != null) {
                        CustomEmojiText(
                            text = message.text ?: "",
                            emojis = message.emojis,
                            accountHost = accountHost,
                            sourceHost = accountHost,
                            fontSize = 16.sp
                        )
                    }
                    if (message.file != null) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3),
                            painter = rememberAsyncImagePainter(message.file?.thumbnailUrl),
                            contentDescription = null
                        )
                    }
                }
            }

            Text(getSimpleElapsedTime(time = message.createdAt))
        }

    }
}

@Composable
@Stable
fun RecipientMessageBubble(
    user: User,
    message: Message,
    accountHost: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        MessageAvatarIcon(avatarUrl = user.avatarUrl)
        Spacer(Modifier.width(8.dp))
        Column {
            CustomEmojiText(
                text = user.displayName,
                emojis = user.emojis,
                fontSize = 16.sp,
                accountHost = accountHost,
                sourceHost = user.host,
            )
            Surface(
                shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                color = MaterialTheme.colors.surface,
                elevation = 4.dp
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    if (message.text != null) {
                        CustomEmojiText(text = message.text ?: "", emojis = message.emojis, accountHost = accountHost, sourceHost = user.host)
                    }
                    if (message.file != null) {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3),
                            painter = rememberAsyncImagePainter(message.file?.thumbnailUrl),
                            contentDescription = null
                        )
                    }

                }

            }
            Text(getSimpleElapsedTime(time = message.createdAt))
        }
        Spacer(Modifier.width(48.dp))


    }
}


@Composable
@Stable
private fun MessageAvatarIcon(avatarUrl: String?) {
    AvatarIcon(url = avatarUrl, size = 50.dp)
}

@Preview
@Composable
fun PreviewMessageBubble() {
    Column {
        SelfMessageBubble(
            Message.Direct.make(
                id = Message.Id(0L, ""),
                recipientId = User.Id(0L, ""),
                userId = User.Id(0L, ""),
                text = "testtesttest"
            ),
            accountHost = null,
        )
        RecipientMessageBubble(
            User.Simple.make(User.Id(0L, ""), "harunon"),
            Message.Direct.make(
                id = Message.Id(0L, ""),
                recipientId = User.Id(0L, ""),
                userId = User.Id(0L, ""),
                text = "testtesttest"
            ),
            accountHost = null,
        )
    }
}