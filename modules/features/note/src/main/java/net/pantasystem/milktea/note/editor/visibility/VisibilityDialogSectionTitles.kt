package net.pantasystem.milktea.note.editor.visibility

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.note.R

@Composable
internal fun VisibilityChannelTitle() {
    Text(
        stringResource(R.string.channel),
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
internal fun ReactionAcceptanceTitle() {
    Text(
        stringResource(R.string.reaction_acceptance),
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        fontWeight = FontWeight.ExtraBold
    )
}

