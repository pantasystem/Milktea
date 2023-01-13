package net.pantasystem.milktea.messaging

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.messaging.*
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.make

sealed interface Action {
    data class OnClick(val history: MessageHistoryRelation) : Action
    data class OnAvatarIconClick(val user: User) : Action
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageHistoryCard(
    history: MessageHistoryRelation,
    onAction: (Action) -> Unit,
    isUserNameDefault: Boolean
) {

    Card(
        onClick = {
            onAction(Action.OnClick(history))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        ) {
            Image(
                rememberAsyncImagePainter(history.thumbnailUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable {
                        onAction(
                            Action.OnAvatarIconClick(
                                when (history) {
                                    is MessageHistoryRelation.Direct -> history.partner
                                    is MessageHistoryRelation.Group -> history.user
                                }
                            )
                        )
                    }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                when (history) {
                    is MessageHistoryRelation.Direct -> {
                        CustomEmojiText(
                            text = history.getTitle(isUserNameDefault),
                            emojis = history.partner.emojis,
                            maxLines = 1,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            accountHost = history.account.getHost(),
                            sourceHost = history.partner.host,
                            parsedResult = if (isUserNameDefault) null else history.partner.parsedResult
                        )
                    }
                    is MessageHistoryRelation.Group -> {
                        Text(
                            history.getTitle(isUserNameDefault),
                            maxLines = 1,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                if (history.message.text != null) {
                    Text(
                        history.message.text ?: ""
                    )
                }


            }
        }

    }

}

@Composable
@Preview
fun PreviewMessageHistoryCard() {

    MessageHistoryCard(
        history = MessageHistoryRelation.Direct(
            account = Account(
                remoteId = "1",
                instanceDomain = "",
                userName = "",
                Account.InstanceType.MISSKEY,
                ""
            ),
            message = Message.Direct(
                Message.Id(0L, ""),
                createdAt = Clock.System.now(),
                emojis = emptyList(),
                file = null,
                fileId = null,
                isRead = true,
                recipientId = User.Id(0L, "2"),
                text = "testtest",
                userId = User.Id(0L, "1")
            ),
            recipient = User.Simple.make(User.Id(0L, "2"), userName = "harunon"),
            user = User.Simple.make(User.Id(0L, "1"), userName = "Klm", name = "しゃも爺")
        ),
        onAction = {},
        isUserNameDefault = false
    )
}