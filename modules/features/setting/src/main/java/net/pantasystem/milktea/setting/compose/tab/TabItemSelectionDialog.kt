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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common_android_ui.account.page.PageTypeHelper
import net.pantasystem.milktea.model.account.page.PageType


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TabItemSelectionDialog(
    modifier: Modifier = Modifier,
    items: List<PageType>,
    onClick: (PageType) -> Unit,
) {
    LazyColumn(
        modifier
    ) {
        items(items) { pageType ->
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
                        PageTypeHelper.nameByPageType(LocalContext.current, pageType),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
