package jp.panta.misskeyandroidclient.ui.channel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.channel.ChannelListType
import jp.panta.misskeyandroidclient.model.channel.ChannelPagingModel
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent

@Composable
fun ChannelListStateScreen(
    account: Account,
    listType: ChannelListType,
    viewModel: ChannelViewModel
) {
    val key = PagingModelKey(account.accountId, listType)

    val pagingState by viewModel.getObservable(key).collectAsState(
        initial = PageableState.Fixed(StateContent.NotExist())
    )

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    LaunchedEffect(key1 = null) {
        viewModel.loadPrevious(key)
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.loadPrevious(key)
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        when (val content = pagingState.content) {
            is StateContent.Exist -> {
                LazyColumn {
                    items(content.rawContent.size) { index ->
                        val channel = content.rawContent[index]
                        val isPaged = account.pages.any { it.pageParams.channelId == channel.id.channelId }
                        ChannelCard(
                            channel = channel,
                            isPaged = isPaged,
                            onAction = {

                            }
                        )
                    }
                    if (pagingState is PageableState.Loading.Previous) {
                        item {
                            ReachedElement()
                        }
                    }
                }
            }
            is StateContent.NotExist -> {
                when (pagingState) {
                    is PageableState.Loading -> {

                    }
                    is PageableState.Fixed -> {

                    }
                    is PageableState.Error -> {

                    }
                }
            }
        }
    }
}

@Composable
fun ReachedElement() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        CircularProgressIndicator()
    }
}