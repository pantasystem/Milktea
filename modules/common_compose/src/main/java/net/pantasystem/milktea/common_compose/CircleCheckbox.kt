package net.pantasystem.milktea.common_compose

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
@Stable
fun CircleCheckbox(modifier: Modifier = Modifier, selected: Boolean) {

    val color = MaterialTheme.colors
    val imageVector = if (selected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
    val tint = color.primary
    val background = if (selected) Color.White else Color.Transparent

    Icon(
        imageVector = imageVector, tint = tint,
        modifier = modifier.background(background, shape = CircleShape),
        contentDescription = "checkbox"
    )
}