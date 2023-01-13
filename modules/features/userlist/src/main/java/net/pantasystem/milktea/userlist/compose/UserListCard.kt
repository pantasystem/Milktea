package net.pantasystem.milktea.userlist.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListMember
import net.pantasystem.milktea.model.list.UserListWithMembers
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.userlist.viewmodel.UserListBindingModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
@Stable
fun UserListCard(userList: UserListBindingModel, onAction: (UserListCardAction) -> Unit) {
    Card(
        onClick = {
            onAction(UserListCardAction.OnClick(userList.userList))
        },
        Modifier
            .fillMaxWidth()
            .padding(0.5.dp),
        backgroundColor = if (userList.isTargetUserAdded) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                    horizontal = 16.dp
                )
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Text(userList.userList.userList.name, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    for (m in userList.userList.members) {
                        Image(
                            rememberAsyncImagePainter(m.avatarUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            AddToTabButton(isPaged = userList.isAddedTab) {
                onAction(UserListCardAction.OnClickToggleTab(userList.userList))
            }
        }

    }
}

@Preview
@Composable
fun Preview_GroupCard() {
    UserListCard(
        userList = UserListBindingModel(
            UserListWithMembers(
                UserList(
                    id = UserList.Id(0, ""),
                    createdAt = Clock.System.now(),
                    name = "はるのんファンクラブ",
                    userIds = (0 until 6).map { User.Id(0L, "$it") }
                ),
                members = listOf(
                    UserListMember(User.Id(0L, "id"), ""),
                    UserListMember(User.Id(0L, "id"), ""),
                    UserListMember(User.Id(0L, "id"), ""),
                    UserListMember(User.Id(0L, "id"), ""),
                    UserListMember(User.Id(0L, "id"), ""),
                )
            ),
            isAddedTab = true,
            isTargetUserAdded = false,
        ),
        onAction = {}
    )

}

@Composable
@Stable
private fun AddToTabButton(modifier: Modifier = Modifier, isPaged: Boolean, onPressed: () -> Unit) {
    IconButton(onClick = onPressed, modifier = modifier) {
        if (isPaged) {
            Icon(
                imageVector = Icons.Default.BookmarkRemove,
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        } else {
            Icon(
                imageVector = Icons.Default.BookmarkAdd,
                contentDescription = "add to tab",
                tint = MaterialTheme.colors.secondary
            )
        }

    }
}

sealed interface UserListCardAction {
    data class OnClick(val userList: UserListWithMembers) : UserListCardAction
    data class OnClickToggleTab(val userList: UserListWithMembers) : UserListCardAction
}