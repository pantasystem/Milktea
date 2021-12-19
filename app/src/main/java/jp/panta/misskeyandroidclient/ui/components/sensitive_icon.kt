package jp.panta.misskeyandroidclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import jp.panta.misskeyandroidclient.R

@Composable
fun SensitiveIcon() {
    Icon(
        painter = painterResource(R.drawable.ic_baseline_hide_image_24),
        contentDescription = stringResource(R.string.sensitive),
        modifier = Modifier.background(MaterialTheme.colors.secondary)
    )
}