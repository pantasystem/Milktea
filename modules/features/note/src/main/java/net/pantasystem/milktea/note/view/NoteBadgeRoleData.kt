package net.pantasystem.milktea.note.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest

private val defaultSvgDecoderFactory = SvgDecoder.Factory()

@Composable
fun NoteBadgeRoles(
    badgeRoles: List<NoteBadgeRoleData>,
    modifier: Modifier = Modifier,
) {
    val sortedBadgeRoles = badgeRoles
        // アイコンなしは出しようがないので弾く
        .filter { it.iconUri != null }
        .sortedBy { it.displayOrder }

    Surface {
        Row(
//            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = modifier.fillMaxHeight()
        ) {
            for (it in sortedBadgeRoles) {
                NoteBadgeRoleImage(
                    data = it,
                    modifier = Modifier.padding(1.dp) // 名前欄とxml側で高さを揃えているが、それでも気持ちデカイので調節
                )
            }
        }
    }
}

@Composable
fun NoteBadgeRoleImage(
    data: NoteBadgeRoleData,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(data.iconUri)
                .decoderFactory(defaultSvgDecoderFactory)
                .build(),
        ),
        contentDescription = data.name,
        contentScale = ContentScale.FillHeight,
        modifier = modifier.aspectRatio(1.0f)
    )
}

data class NoteBadgeRoleData(
    val name: String,
    val iconUri: String?,
    val displayOrder: Int,
)