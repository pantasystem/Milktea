package net.pantasystem.milktea.common_android_ui.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountInfo
import net.pantasystem.milktea.common_compose.AvatarIcon
import net.pantasystem.milktea.common_compose.CustomEmojiText

@Composable
fun AccountTile(
    modifier: Modifier = Modifier,
    account: AccountInfo,
    onClick: (AccountInfo) -> Unit,
    onAvatarClick: (AccountInfo) -> Unit,
) {
    Surface(
        modifier = modifier.clickable {
            onClick(account)
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                    horizontal = 16.dp
                ),
        ) {
            AvatarIcon(
                url = account.user?.avatarUrl,
                size = 56.dp,
                onAvatarClick = {
                    onAvatarClick(account)
                },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (account.user?.name != null) {
                        CustomEmojiText(
                            text = account.user.name ?: "", emojis = account.user.emojis,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            accountHost = account.account.getHost(),
                            sourceHost = account.user.host,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        account.user?.shortDisplayName ?: account.account.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (account.instanceMeta?.iconUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(account.instanceMeta.iconUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(14.dp)
                                .clip(
                                    RoundedCornerShape(4.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(account.account.getHost())
                }
            }

            CircleCheckbox(
                selected = account.isCurrentAccount,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}


@Composable
@Stable
private fun CircleCheckbox(modifier: Modifier = Modifier, selected: Boolean) {

    val color = MaterialTheme.colors
    val imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
    val tint = color.primary
    val background = if (selected) Color.White else Color.Transparent

    Icon(
        imageVector = imageVector, tint = tint,
        modifier = modifier.background(background, shape = CircleShape),
        contentDescription = "checkbox"
    )
}
