package jp.panta.misskeyandroidclient.ui.messaging

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.make
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make

@Composable
@Stable
fun SelfMessageBubble(
    user: User,
    message: Message,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp),
            contentColor = MaterialTheme.colors.primary,
        ) {
            Column(
                Modifier.padding(8.dp)
            ) {
                if (message.text != null) {
                    Text(text = message.text ?: "")
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        MessageAvatarIcon(avatarUrl = user.avatarUrl)
    }
}

@Composable
@Stable
fun RecipientMessageBubble(
    user: User,
    message: Message,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        MessageAvatarIcon(avatarUrl = user.avatarUrl)
        Spacer(Modifier.width(8.dp))
        Column() {
            CustomEmojiText(text = user.displayName, emojis = user.emojis)
            Surface(
                shape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp),
                contentColor = MaterialTheme.colors.surface,
            ) {
                Column(
                    Modifier.padding(8.dp)
                ) {
                    if (message.text != null) {
                        Text(text = message.text ?: "")
                    }
                    if (message.file != null) {
                        Image(painter = rememberAsyncImagePainter(message.file?.thumbnailUrl), contentDescription = null)
                    }
                }

            }
        }

    }
}



@Composable
@Stable
private fun MessageAvatarIcon(avatarUrl: String?) {
    Image(
        painter = rememberAsyncImagePainter(avatarUrl),
        contentDescription = null,
        modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
    )
}

@Preview
@Composable
fun PreviewMessageBubble() {
    Column {
        SelfMessageBubble(
            User.Simple.make(User.Id(0L, ""), "harunon"),
            Message.Direct.make(
                id = Message.Id(0L, ""),
                recipientId = User.Id(0L, ""),
                userId = User.Id(0L, ""),
                text = "testtesttest"
            )
        )
        RecipientMessageBubble(
            User.Simple.make(User.Id(0L, ""), "harunon"),
            Message.Direct.make(
                id = Message.Id(0L, ""),
                recipientId = User.Id(0L, ""),
                userId = User.Id(0L, ""),
                text = "testtesttest"
            )
        )
    }
}