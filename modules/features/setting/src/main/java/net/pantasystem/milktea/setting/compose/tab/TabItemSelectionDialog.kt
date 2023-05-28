package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import getStringFromStringSource
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.viewmodel.page.PageCandidate


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TabItemSelectionDialog(
    modifier: Modifier = Modifier,
    items: List<PageCandidate>,
    onClick: (PageType) -> Unit,
) {
    LazyColumn(
        modifier
    ) {
        items(items) { pageType ->
            Surface(
                onClick = {
                    onClick(pageType.type)
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
