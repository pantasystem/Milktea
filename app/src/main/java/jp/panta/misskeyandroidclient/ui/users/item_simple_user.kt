package jp.panta.misskeyandroidclient.ui.users

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import jp.panta.misskeyandroidclient.ui.components.CustomEmojiText
import net.pantasystem.milktea.model.user.User

@Composable
fun SimpleUserListView(users: List<User>, onSelected: (User.Id) -> Unit) {
    LazyColumn {
        items(count = users.size) { index ->
            ItemSimpleUserCard(user = users[index], onSelected = onSelected)
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemSimpleUserCard(
    user: User,
    onSelected: (User.Id) -> Unit,
) {

    Card(
        onClick = {
            onSelected.invoke(user.id)
        },
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Image(
                painter = rememberImagePainter(user.avatarUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                CustomEmojiText(text = user.getDisplayName(), emojis = user.emojis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = user.getDisplayUserName())
            }
        }
    }
}





@Preview
@Composable
fun PreviewItemSimpleUser() {
    ItemSimpleUserCard(user = User.Simple(
        avatarUrl = "https://pbs.twimg.com/profile_images/1377726964404908032/nHtGMU-X_400x400.jpg",
        userName = "harunon",
        name = null,
        emojis = emptyList(),
        host = null,
        id = User.Id(0L, ""),
        isBot = true,
        isCat = true,
        nickname = null,
    ), onSelected = {})
}