package net.pantasystem.milktea.user.follow_requests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.user.User

@Composable
fun FollowRequestsScreen(
    uiState: FollowRequestsUiState,
    onAccept: (User.Id) -> Unit,
    onReject: (User.Id) -> Unit,
    onAvatarClicked: (User.Id) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = uiState.pagingState is PageableState.Loading.Init
        ),
        onRefresh = onRefresh
    ) {
        LazyColumn(modifier) {
            when(val content = uiState.pagingState.content) {
                is StateContent.Exist -> {
                    if (content.rawContent.isEmpty()) {
                        item {
                            ContentNone()
                        }
                    } else {
                        items(content.rawContent) { item ->
                            FollowRequestItem(
                                currentAccount = uiState.currentAccount,
                                user = item,
                                onAccept = onAccept,
                                onReject = onReject,
                                onAvatarClicked = onAvatarClicked
                            )
                        }
                    }
                }
                is StateContent.NotExist -> {
                    item {
                        ContentNone()
                    }
                }
            }
            if (uiState.pagingState is PageableState.Loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentNone() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Follow requests not exists.")
    }
}