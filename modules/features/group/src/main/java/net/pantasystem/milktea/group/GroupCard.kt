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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupMember
import net.pantasystem.milktea.model.group.GroupWithMember
import net.pantasystem.milktea.model.user.User

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Stable
fun GroupCard(group: GroupWithMember, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        Modifier
            .fillMaxWidth()
            .padding(0.5.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                    horizontal = 16.dp
                )
        ) {
            Text(group.group.name, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                for (m in group.members) {
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
    }
}

@Preview
@Composable
fun Preview_GroupCard() {
    GroupCard(
        group = GroupWithMember(
            Group(
                id = Group.Id(0, ""),
                createdAt = Clock.System.now(),
                name = "はるのんファンクラブ",
                ownerId = User.Id(0, ""),
                userIds = (0 until 6).map { User.Id(0L, "$it") }
            ),
            members = listOf(
                GroupMember(User.Id(0L, "id"), ""),
                GroupMember(User.Id(0L, "id"), ""),
                GroupMember(User.Id(0L, "id"), ""),
                GroupMember(User.Id(0L, "id"), ""),
                GroupMember(User.Id(0L, "id"), ""),
            )
        ),
        onClick = {}
    )

}