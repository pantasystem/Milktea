package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common_compose.AvatarIcon
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

            AvatarIcon(
                url = avatarUrl,
                onAvatarClick = onClick,
                size = 48.dp,
                borderStrokeWidth = 1.dp,
                borderStrokeColor = Color.Gray,
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