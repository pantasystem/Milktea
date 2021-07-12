package jp.panta.misskeyandroidclient.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import jp.panta.misskeyandroidclient.R

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    onClick: ()->Unit,
) {
    val iconResource = if(isFavorite) {
        painterResource(id = R.drawable.ic_baseline_red_favorite_24)
    }else{
        painterResource(id = R.drawable.ic_baseline_favorite_border_24)
    }
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(painter = iconResource, contentDescription = null)
    }

}

@Preview
@Composable
fun PreviewFavoriteButton() {
    FavoriteButton(isFavorite = true) {

    }
}