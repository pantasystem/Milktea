package net.pantasystem.milktea.userlist.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.model.user.User


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RemovableSimpleUserCard(
    user: User,
    accountHost: String?,
    onSelected: (User) -> Unit,
    onDeleteButtonClicked: (User) -> Unit,
) {

    Card(
        onClick = {
            onSelected.invoke(user)
        },
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.avatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    CustomEmojiText(
                        text = user.displayName,
                        emojis = user.emojis,
                        sourceHost = user.host,
                        accountHost = accountHost,
                        parsedResult = user.parsedResult,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user.displayUserName)
                }
            }
            IconButton(
                onClick = {
                    onDeleteButtonClicked(user)
                },

                ) {

                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }

    }
}
