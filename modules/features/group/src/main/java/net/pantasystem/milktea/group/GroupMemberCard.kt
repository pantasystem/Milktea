package net.pantasystem.milktea.group

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.common_compose.AvatarIcon
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
            AvatarIcon(
                member.avatarUrl,
                size = 50.dp,
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