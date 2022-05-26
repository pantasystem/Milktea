package net.pantasystem.milktea.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.common.R

@Composable
fun SensitiveIcon() {
    Icon(
        Icons.Default.HideImage,
        contentDescription = stringResource(R.string.sensitive),
        modifier = Modifier.background(MaterialTheme.colors.secondary)
    )
}