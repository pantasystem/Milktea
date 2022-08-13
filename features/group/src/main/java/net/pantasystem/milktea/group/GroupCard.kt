package net.pantasystem.milktea.group

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import net.pantasystem.milktea.model.group.GroupWithMember

@OptIn(ExperimentalMaterialApi::class)
@Composable
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
                .padding(8.dp)
        ) {
            Text(group.group.name, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                for (m in group.members) {
                    Image(
                        rememberAsyncImagePainter(m.avatarUrl),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}