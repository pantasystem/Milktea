package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter


@Composable
fun AvatarIcon(
    url: String?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = Shape.Undefined,
    size: Dp = 50.dp,
    borderStrokeWidth: Dp = 0.dp,
    borderStrokeColor: Color = Color.Transparent
) {
    val shapeLocal = LocalAvatarIconShape.current
    val clip = when(shape) {
        Shape.Circle -> CircleShape
        Shape.RoundedCorner -> RoundedCornerShape(16.dp)
        Shape.Undefined -> when(shapeLocal) {
            Shape.Circle -> CircleShape
            Shape.RoundedCorner -> RoundedCornerShape(16.dp)
            Shape.Undefined -> CircleShape
        }
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(clip)
            .border(borderStrokeWidth, borderStrokeColor, clip)
            .clickable {
                onAvatarClick()
            },
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun AvatarIcon(
    url: String?,
    modifier: Modifier = Modifier,
    shape: Shape = Shape.Undefined,
    size: Dp = 50.dp,
    borderStrokeWidth: Dp = 0.dp,
    borderStrokeColor: Color = Color.Transparent
) {
    val shapeLocal = LocalAvatarIconShape.current
    val clip = when(shape) {
        Shape.Circle -> CircleShape
        Shape.RoundedCorner -> RoundedCornerShape(16.dp)
        Shape.Undefined -> when(shapeLocal) {
            Shape.Circle -> CircleShape
            Shape.RoundedCorner -> RoundedCornerShape(16.dp)
            Shape.Undefined -> CircleShape
        }
    }
    Image(
        modifier = modifier
            .size(size)
            .clip(clip)
            .border(borderStrokeWidth, borderStrokeColor, clip),
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}
enum class Shape {
    Circle,
    RoundedCorner,
    Undefined
}

val LocalAvatarIconShape = compositionLocalOf<Shape> {
    Shape.Circle
}