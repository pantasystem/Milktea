package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.common_compose.getSimpleElapsedTime
import net.pantasystem.milktea.model.user.User

@ExperimentalCoroutinesApi
@Composable
@Stable
fun ItemRenoteUser(
    note: RenoteItemType,
    myId: User.Id?,
    accountHost: String?,
    onAction: (ItemRenoteAction) -> Unit,
    isUserNameDefault: Boolean = false
) {

    val createdAt = (note as? RenoteItemType.Renote)?.let {
        getSimpleElapsedTime(time = it.note.note.createdAt)
    }

    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .padding(0.5.dp)
            .clickable {
                onAction(ItemRenoteAction.OnClick(note))
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(note.user.avatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if(isUserNameDefault){
                        Text(text = note.user.displayUserName, fontSize = 16.sp)
                    }else{
                        CustomEmojiText(
                            text = note.user.displayName,
                            emojis = note.user.emojis,
                            fontSize = 16.sp,
                            parsedResult = note.user.parsedResult,
                            accountHost = accountHost,
                            sourceHost = note.user.host,
                        )
                    }
                    if(isUserNameDefault){
                        CustomEmojiText(
                            text = note.user.displayName,
                            emojis = note.user.emojis,
                            accountHost = accountHost,
                            sourceHost = note.user.host,
                            parsedResult = note.user.parsedResult,
                        )
                    }else{
                        Text(text = note.user.displayUserName)
                    }
                }
            }
            Column {
                if (createdAt != null) {
                    Text(createdAt)
                }
                if (note.user.id == myId) {
                    IconButton(onClick = {
                        onAction(ItemRenoteAction.OnDeleteButtonClicked(note))
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }

            }

        }
    }
}


sealed interface ItemRenoteAction {
    data class OnClick(val note: RenoteItemType) : ItemRenoteAction
    data class OnDeleteButtonClicked(val note: RenoteItemType): ItemRenoteAction
}

