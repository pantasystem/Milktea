package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.common_compose.AvatarIcon
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.common_compose.getSimpleElapsedTime
import net.pantasystem.milktea.model.user.User
import java.text.SimpleDateFormat
import java.util.Date

@ExperimentalCoroutinesApi
@Composable
@Stable
fun ItemRenoteUser(
    note: RenoteItemType,
    myId: User.Id?,
    accountHost: String?,
    isDisplayTimestampsAsAbsoluteDates: Boolean,
    onAction: (ItemRenoteAction) -> Unit,
    isUserNameDefault: Boolean = false
) {

    val createdAt = (note as? RenoteItemType.Renote)?.let { renote ->
        if (isDisplayTimestampsAsAbsoluteDates) {
            remember(renote.note.note.createdAt) {
                SimpleDateFormat.getDateTimeInstance().format(renote.note.note.createdAt.let {
                    Date(it.toEpochMilliseconds())
                })
            }
        } else {
            getSimpleElapsedTime(time = renote.note.note.createdAt)
        }

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
                AvatarIcon(url = note.user.avatarUrl, size = 48.dp)
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

