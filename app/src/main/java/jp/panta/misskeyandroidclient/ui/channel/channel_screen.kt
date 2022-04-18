package jp.panta.misskeyandroidclient.ui.channel

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.data.model.channel.impl.ChannelListType
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.account.AccountStore

data class ChannelTypeWithTitle(
    val type: ChannelListType,
    val title: String,
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChannelScreen(
    onNavigateUp: () -> Unit,
    accountStore: AccountStore,
    channelViewModel: ChannelViewModel,
) {

    val currentAccount by accountStore.observeCurrentAccount.collectAsState(initial = null)
    val channelTypeWithTitleList = listOf(
        ChannelTypeWithTitle(ChannelListType.FEATURED, stringResource(id = R.string.featured)),
        ChannelTypeWithTitle(ChannelListType.FOLLOWED, stringResource(id = R.string.following)),
        ChannelTypeWithTitle(ChannelListType.OWNED, stringResource(id = R.string.owned))
    )

    val pagerState = rememberPagerState(pageCount = channelTypeWithTitleList.size)
    val coroutine = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp.invoke()
                            },
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        Text(stringResource(id = R.string.channel))
                    },
                    elevation = 0.dp
                )
                if (currentAccount != null) {
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        channelTypeWithTitleList.forEachIndexed { i, item ->

                            Tab(
                                selected = pagerState.currentPage == i,
                                onClick = { coroutine.launch { pagerState.animateScrollToPage(i) } },
                                text = { Text(item.title) }
                            )
                        }
                    }
                }

            }
        }
    ) {
        if (currentAccount == null) {
            CircularProgressIndicator()
        } else {
            HorizontalPager(state = pagerState) {
                ChannelListStateScreen(
                    listType = channelTypeWithTitleList[pagerState.currentPage].type,
                    account = currentAccount!!,
                    viewModel = channelViewModel,
                )
            }
        }

    }
}