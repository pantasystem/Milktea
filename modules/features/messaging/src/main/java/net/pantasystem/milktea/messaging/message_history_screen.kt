package net.pantasystem.milktea.messaging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.messaging.viewmodel.MessageHistoryViewModel
import net.pantasystem.milktea.model.messaging.messagingId

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageHistoryScreen(
    historyViewModel: MessageHistoryViewModel,
    onAction: (Action) -> Unit,
) {

    val uiState by historyViewModel.uiState.collectAsState()
    val isRefreshing by historyViewModel.isRefreshing.observeAsState()

    LaunchedEffect(key1 = null) {
        historyViewModel.loadGroupAndUser()
    }

    SwipeRefresh(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        state = rememberSwipeRefreshState(
                isRefreshing = isRefreshing ?: false
        ),
        onRefresh = { historyViewModel.loadGroupAndUser() }
    ) {
        when(val content = uiState.histories.content) {
            is StateContent.Exist -> {
                val list = content.rawContent

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(content.rawContent.size, key = { list[it].messagingId }) { i ->
                        MessageHistoryCard(
                            history = list[i],
                            isUserNameDefault = uiState.isUserNameDefault,
                            onAction = onAction,
                        )
                    }
                }

            }
            is StateContent.NotExist -> {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (val state = uiState.histories) {
                        is ResultState.Error -> {
                            Text("Load Error")
                            Text(state.throwable.toString())
                        }
                        is ResultState.Fixed -> {
                            Text("No content")
                        }
                        is ResultState.Loading -> {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }

    }

}