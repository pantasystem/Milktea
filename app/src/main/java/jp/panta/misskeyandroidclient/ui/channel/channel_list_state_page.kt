package jp.panta.misskeyandroidclient.ui.channel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.channel.Channel
import net.pantasystem.milktea.data.model.channel.ChannelListType
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent

@Composable
fun ChannelListStateScreen(
    account: Account,
    listType: ChannelListType,
    viewModel: ChannelViewModel,
    navigateToDetailView: (Channel.Id) -> Unit = {}
) {
    val key = PagingModelKey(account.accountId, listType)

    val pagingState by viewModel.getObservable(key).collectAsState(
        initial = net.pantasystem.milktea.common.PageableState.Fixed(net.pantasystem.milktea.common.StateContent.NotExist())
    )

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    LaunchedEffect(true) {
        viewModel.clearAndLoad(key)
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.clearAndLoad(key)
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (val content = pagingState.content) {
            is net.pantasystem.milktea.common.StateContent.Exist -> {
                LazyColumn {
                    items(content.rawContent.size) { index ->
                        val channel = content.rawContent[index]
                        val isPaged =
                            account.pages.any { it.pageParams.channelId == channel.id.channelId }
                        ChannelCard(
                            channel = channel,
                            isPaged = isPaged,
                            onAction = {
                                when (it) {
                                    is ChannelCardAction.OnToggleTabButtonClicked -> {
                                        viewModel.toggleTab(it.channel.id)
                                    }
                                    is ChannelCardAction.OnUnFollowButtonClicked -> {
                                        viewModel.unFollow(it.channel.id)
                                    }
                                    is ChannelCardAction.OnFollowButtonClicked -> {
                                        viewModel.follow(it.channel.id)
                                    }
                                    is ChannelCardAction.OnClick -> {
                                        navigateToDetailView.invoke(channel.id)
                                    }
                                }
                            }
                        )
                    }
                    if (pagingState is net.pantasystem.milktea.common.PageableState.Loading.Previous) {
                        item {
                            ReachedElement()
                        }
                    }
                }
            }
            is net.pantasystem.milktea.common.StateContent.NotExist -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (pagingState) {
                        is net.pantasystem.milktea.common.PageableState.Loading -> {
                            CircularProgressIndicator()
                        }
                        is net.pantasystem.milktea.common.PageableState.Fixed -> {
                            Text("no contents")
                        }
                        is net.pantasystem.milktea.common.PageableState.Error -> {
                            Text("error:${((pagingState as net.pantasystem.milktea.common.PageableState.Error).throwable)}")
                        }
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