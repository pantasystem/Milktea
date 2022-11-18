package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun RenoteTargetAccountRowList(
    accounts: List<AccountWithUser>
) {

}

@Composable
fun SelectableAvatarOnlyAccount(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    avatarUrl: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.padding(4.dp),
        contentAlignment = Alignment.TopEnd,
    ) {

        Image(
            painter = rememberAsyncImagePainter(avatarUrl),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .clip(CircleShape)
                .size(48.dp)
                .clickable { onClick() }
        )
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
        } else {
            Icon(
                Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
        }
    }
}

@Composable
@Preview
fun PreviewSelectableAvatarOnlyAccount() {
    SelectableAvatarOnlyAccount(
        avatarUrl = "",
        onClick = {},
        isSelected = true
    )
}