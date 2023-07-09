package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getStringFromStringSource
import net.pantasystem.milktea.setting.viewmodel.page.PageCandidate
import net.pantasystem.milktea.setting.viewmodel.page.PageCandidateGroup


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun TabItemSelectionDialog(
    modifier: Modifier = Modifier,
    items: List<PageCandidateGroup>,
    onClick: (PageCandidate) -> Unit,
) {
    LazyColumn(
        modifier
    ) {
        for (group in items) {
            stickyHeader {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.surface,
                ) {
                    Text(
                        "@${group.relatedAccount.userName}@${group.relatedAccount.getHost()}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            items(group.candidates) { pageType ->
                Surface(
                    onClick = {
                        onClick(pageType)
                    },
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            getStringFromStringSource(pageType.name),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

    }
}
