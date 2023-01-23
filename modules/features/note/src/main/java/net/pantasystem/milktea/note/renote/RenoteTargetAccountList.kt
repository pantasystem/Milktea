package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.emoji.Emoji

@Composable
fun RenoteTargetAccountRowList(
    modifier: Modifier = Modifier,
    accounts: List<AccountWithUser>,
    onClick: (Long) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        accounts.forEach {
            SelectableAvatarOnlyAccount(
                isSelected = it.isSelected,
                avatarUrl = it.user.avatarUrl ?: "",
                onClick = {
                    onClick(it.accountId)
                },
                username = it.user.displayName,
                emojis = it.user.emojis,
                isEnable = it.isEnable,
                accountHost = it.user.host,
            )
        }
    }
}

@Composable
fun SelectableAvatarOnlyAccount(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    isEnable: Boolean,
    username: String,
    avatarUrl: String,
    emojis: List<Emoji>,
    accountHost: String?,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(56.dp)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(

            contentAlignment = Alignment.TopEnd,
        ) {

            Image(
                painter = rememberAsyncImagePainter(avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(48.dp)
                    .clickable { onClick() }
                    .background(Color.Black)
            )
            val tint = MaterialTheme.colors.primary
            val background = if (isSelected) Color.White else Color.Transparent

            if (isEnable) {
                if (isSelected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.background(background, CircleShape)

                    )
                } else {
                    Icon(
                        Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.background(background, CircleShape)
                    )

                }
            }


        }
        CustomEmojiText(text = username, emojis = emojis, maxLines = 1, fontSize = 8.sp, accountHost = accountHost, sourceHost = accountHost)
    }

}

@Composable
@Preview
fun PreviewSelectableAvatarOnlyAccount() {
    SelectableAvatarOnlyAccount(
        avatarUrl = "",
        onClick = {},
        isSelected = true,
        username = "@harunon",
        emojis = emptyList(),
        isEnable = true,
        accountHost = "misskey.io"
    )
}