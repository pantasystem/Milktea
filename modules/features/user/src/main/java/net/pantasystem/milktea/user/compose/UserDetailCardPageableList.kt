package net.pantasystem.milktea.user.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrollToTheEnd
import net.pantasystem.milktea.model.user.User

sealed interface UserDetailCardPageableListAction {
    data class CardAction(
        val cardAction: UserDetailCardAction
    ) : UserDetailCardPageableListAction

    object OnBottomReached : UserDetailCardPageableListAction
    object Refresh : UserDetailCardPageableListAction
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserDetailCardPageableList(
    pageableState: PageableState<List<User.Id>>,
    users: List<User.Detail>,
    isUserNameMain: Boolean,
    accountHost: String?,
    myId: String?,
    onAction: (UserDetailCardPageableListAction) -> Unit,
) {
    val scrollController = rememberLazyStaggeredGridState()
    LaunchedEffect(key1 = null) {
        snapshotFlow {
            scrollController.isScrollToTheEnd()
        }.distinctUntilChanged().onEach {
            if (it) {
                onAction(UserDetailCardPageableListAction.OnBottomReached)
            }
        }.launchIn(this)
    }

    when (pageableState.content) {
        is StateContent.Exist -> {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = pageableState is PageableState.Loading.Init),
                onRefresh = { onAction(UserDetailCardPageableListAction.Refresh) },
                modifier = Modifier
                    .nestedScroll(rememberNestedScrollInteropConnection())
                    .fillMaxSize()
            ) {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Adaptive(350.dp), state = scrollController) {
                    items(count = users.size) { i ->
                        UserDetailCard(
                            userDetail = users[i],
                            isUserNameMain = isUserNameMain,
                            onAction = {
                                onAction(UserDetailCardPageableListAction.CardAction(it))
                            },
                            accountHost = accountHost,
                            myId = myId,
                        )
                    }

                    item(span = StaggeredGridItemSpan.FullLine){
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
        is StateContent.NotExist -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (pageableState) {
                    is PageableState.Error -> {
                        Text("Error")
                        Text(pageableState.throwable.toString())
                    }
                    is PageableState.Fixed -> {
                        Text("Content is empty")
                    }
                    is PageableState.Loading -> {
                        CircularProgressIndicator()
                    }
                }
            }

        }
    }

}