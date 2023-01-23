package net.pantasystem.milktea.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.model.user.User

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Stable
fun GroupMemberCard(
    member: User,
    ownerId: User.Id?,
    isOwnGroup: Boolean,
    onAction: (GroupMemberCardAction) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(0.5.dp)
            .fillMaxWidth(),
        onClick = {
            onAction(GroupMemberCardAction.OnClick(member))
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                rememberAsyncImagePainter(member.avatarUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(member.displayName)
                Text(member.displayUserName)
            }

        }
    }
}

sealed interface GroupMemberCardAction {
    data class OnClick(val user: User) : GroupMemberCardAction
    data class RejectMember(val user: User) : GroupMemberCardAction
}