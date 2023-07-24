package net.pantasystem.milktea.user.followrequests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.R

@Composable
fun FollowRequestsScreen(
    uiState: FollowRequestsUiState,
    onAccept: (User.Id) -> Unit,
    onReject: (User.Id) -> Unit,
    onAvatarClicked: (User.Id) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(null) {
        onRefresh()
    }
    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = uiState.pagingState is PageableState.Loading.Init
        ),
        onRefresh = onRefresh
    ) {
        LazyColumn(modifier) {
            when(val content = uiState.pagingState.content) {
                is StateContent.Exist -> {
                    if (content.rawContent.isNotEmpty()) {
                        items(content.rawContent) { item ->
                            FollowRequestItem(
                                currentAccount = uiState.currentAccount,
                                user = item,
                                isUserNameDefault = uiState.config.isUserNameDefault,
                                onAccept = onAccept,
                                onReject = onReject,
                                onAvatarClicked = onAvatarClicked
                            )
                        }
                    }
                }
                is StateContent.NotExist -> Unit
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    when(uiState.pagingState) {
                        is PageableState.Error -> {
                            Text(stringResource(id = R.string.error_s, uiState.pagingState.throwable.localizedMessage ?: ""))
                        }
                        is PageableState.Fixed -> {
                            val showMessage = when(val content = uiState.pagingState.content) {
                                is StateContent.Exist -> {
                                    content.rawContent.isEmpty()
                                }
                                is StateContent.NotExist -> true
                            }
                            if (showMessage) {
                                Text(stringResource(id = R.string.content_not_exists_message))
                            }
                        }
                        is PageableState.Loading -> {
                            CircularProgressIndicator()
                        }
                    }
                }

            }
        }
    }
}
