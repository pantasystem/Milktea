package net.pantasystem.milktea.search.trend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.model.hashtag.HashTag
import net.pantasystem.milktea.search.R

@Composable
fun HashtagTrendItem(
    hashtag: HashTag,
    onClick: () -> Unit,
) {

    Surface(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                    horizontal = 14.dp
                )
        ) {
            Text(
                "#${hashtag.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(id = R.string.trend_posted_person_count_msg, hashtag.usersCount))
        }
    }
}