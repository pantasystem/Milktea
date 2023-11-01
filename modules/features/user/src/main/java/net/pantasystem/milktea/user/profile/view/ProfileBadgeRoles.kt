package net.pantasystem.milktea.user.profile.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest

private val defaultSvgDecoderFactory = SvgDecoder.Factory()

@Composable
fun ProfileBadgeRoles(
    badgeRoles: List<ProfileBadgeRoleData>
) {
    val sortedBadgeRoles = badgeRoles.sortedBy { it.displayOrder }

    Surface(
        color = MaterialTheme.colors.primary,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (it in sortedBadgeRoles) {
                ProfileBadgeRole(it)
            }
        }
    }
}

@Composable
fun ProfileBadgeRole(
    data: ProfileBadgeRoleData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .border(
                color = Color.LightGray,
                width = 0.5.dp,
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            ConstraintLayout {
                val (image, text) = createRefs()

                ProfileBadgeRoleImage(
                    data = data,
                    modifier = Modifier
                        .constrainAs(image) {
                            // startの位置だけ固定して、高さはラベルと合わせる
                            top.linkTo(text.top)
                            bottom.linkTo(text.bottom)
                            start.linkTo(parent.start)

                            width = Dimension.wrapContent
                            height = Dimension.fillToConstraints
                        }
                        .padding(2.dp) // textと高さを揃えているが、それでも気持ちデカイので調節
                )

                ProfileBadgeRoleText(
                    data = data,
                    modifier = Modifier
                        .constrainAs(text) {
                            // startの位置だけ固定して、高さ・幅は中の文字列とフォントの高さにまかせる
                            start.linkTo(image.end)

                            width = Dimension.wrapContent
                            height = Dimension.wrapContent
                        }
                )
            }
        }
    }
}

@Composable
fun ProfileBadgeRoleImage(data: ProfileBadgeRoleData, modifier: Modifier = Modifier) {
    data.iconUri?.let {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(data.iconUri)
                    .decoderFactory(defaultSvgDecoderFactory)
                    .build(),
            ),
            contentDescription = data.name,
            modifier = modifier.aspectRatio(1.0f)
        )
    }
}

@Composable
fun ProfileBadgeRoleText(data: ProfileBadgeRoleData, modifier: Modifier = Modifier) {
    Text(
        text = data.name,
        fontSize = TextUnit(13F, TextUnitType.Sp),
        modifier = modifier.wrapContentHeight()
    )
}

data class ProfileBadgeRoleData(
    val name: String,
    val iconUri: String?,
    val displayOrder: Int,
)