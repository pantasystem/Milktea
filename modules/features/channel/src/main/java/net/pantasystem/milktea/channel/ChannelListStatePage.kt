package net.pantasystem.milktea.channel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.infrastructure.channel.ChannelListType
import net.pantasystem.milktea.model.channel.Channel

@Composable
fun ChannelListStateScreen(
    uiState: ChannelListUiState,
    listType: ChannelListType,
    viewModel: ChannelViewModel,
    navigateToDetailView: (Channel.Id) -> Unit = {}
) {


    val pagingState = uiState.getByType(listType)

    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    LaunchedEffect(listType) {
        viewModel.clearAndLoad(listType)
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.clearAndLoad(listType)
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            when (val content = pagingState.content) {
                is StateContent.Exist -> {
                    items(content.rawContent.size) { index ->
                        val channel = content.rawContent[index]
                        ChannelCard(
                            channel = channel.channel,
                            isPaged = channel.isAddedTab,
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
                                        navigateToDetailView.invoke(channel.channel.id)
                                    }
                                }
                            }
                        )
                    }
                }
                is StateContent.NotExist -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when (pagingState) {
                                is PageableState.Loading -> {
                                    ReachedElement()
                                }
                                is PageableState.Fixed -> {
                                    Text("no contents")
                                }
                                is PageableState.Error -> {
                                    Text("error:${(pagingState.throwable)}")
                                }
                            }
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
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        CircularProgressIndicator()
    }
}