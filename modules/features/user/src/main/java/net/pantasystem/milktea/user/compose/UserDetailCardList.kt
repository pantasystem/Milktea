package net.pantasystem.milktea.user.compose


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.user.User

sealed interface UserDetailCardListAction {
    data class CardAction(
        val cardAction: UserDetailCardAction
    ) : UserDetailCardListAction

    object Refresh : UserDetailCardListAction
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserDetailCardList(
    pageableState: ResultState<List<User.Id>>,
    users: List<User.Detail>,
    accountHost: String?,
    myId: String?,
    isUserNameMain: Boolean,
    onAction: (UserDetailCardListAction) -> Unit,
) {
    val scrollController = rememberLazyListState()


    when (pageableState.content) {
        is StateContent.Exist -> {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = pageableState is ResultState.Loading),
                onRefresh = { onAction(UserDetailCardListAction.Refresh) },
                modifier = Modifier
                    .nestedScroll(rememberNestedScrollInteropConnection())
                    .fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollController) {
                    items(count = users.size) { i ->
                        UserDetailCard(
                            userDetail = users[i],
                            isUserNameMain = isUserNameMain,
                            accountHost = accountHost,
                            myId = myId,
                            onAction = {
                                onAction(UserDetailCardListAction.CardAction(it))
                            },
                        )
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
                    is ResultState.Error -> {
                        Text("Error")
                        Text(pageableState.throwable.toString())
                    }
                    is ResultState.Fixed -> {
                        Text("Content is empty")
                    }
                    is ResultState.Loading -> {
                        CircularProgressIndicator()
                    }
                }
            }

        }
    }

}