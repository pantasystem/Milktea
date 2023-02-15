package net.pantasystem.milktea.channel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.data.infrastructure.channel.ChannelListType
import net.pantasystem.milktea.model.channel.Channel

data class ChannelTypeWithTitle(
    val type: ChannelListType,
    val title: String,
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ChannelScreen(
    onNavigateUp: () -> Unit,
    onNavigateChannelDetail: (channel: Channel.Id) -> Unit,
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

    val uiState by channelViewModel.uiState.collectAsState()

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
    ) { padding ->
        if (currentAccount == null) {
            CircularProgressIndicator(
                modifier = Modifier.padding(padding)
            )
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) {
                ChannelListStateScreen(
                    listType = channelTypeWithTitleList[pagerState.currentPage].type,
                    account = currentAccount!!,
                    viewModel = channelViewModel,
                    navigateToDetailView = onNavigateChannelDetail,
                    uiState = uiState
                )
            }
        }

    }
}