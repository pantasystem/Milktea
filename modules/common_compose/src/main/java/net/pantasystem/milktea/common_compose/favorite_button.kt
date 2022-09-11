package net.pantasystem.milktea.common_compose

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: ()->Unit,
) {
    val iconResource = if(isFavorite) {
        Icons.Default.Favorite
    }else{
        Icons.Default.FavoriteBorder
    }
    IconButton(onClick = onClick, modifier = modifier) {
        Color.Blue
        Icon(iconResource, contentDescription = null, tint = Color(0xFFFF655B))
    }

}

@Preview
@Composable
fun PreviewFavoriteButton() {
    FavoriteButton(isFavorite = true) {

    }
}