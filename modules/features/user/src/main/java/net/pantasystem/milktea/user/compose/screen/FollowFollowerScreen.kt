package net.pantasystem.milktea.user.compose.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.compose.UserDetailCardAction
import net.pantasystem.milktea.user.compose.UserDetailCardPageableList
import net.pantasystem.milktea.user.compose.UserDetailCardPageableListAction
import net.pantasystem.milktea.user.viewmodel.FollowFollowerUiState
import net.pantasystem.milktea.user.viewmodel.FollowFollowerViewModel
import net.pantasystem.milktea.user.viewmodel.LoadType

@Composable
fun FollowFollowerRoute(
    initialTabIndex: Int,
    followFollowerViewModel: FollowFollowerViewModel,
    onCardAction: (UserDetailCardAction) -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by followFollowerViewModel.uiState.collectAsState()

    LaunchedEffect(null) {
        followFollowerViewModel.loadInit()
    }

    FollowFollowerScreen(
        uiState = uiState,
        initialTabIndex = initialTabIndex,
        onLoadInit = {
            followFollowerViewModel.loadInit()
        },
        onLoadPrevious = {
            followFollowerViewModel.loadOld(it)
        },
        onNavigateUp = onNavigateUp,
        onCardAction = onCardAction
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FollowFollowerScreen(
    uiState: FollowFollowerUiState,
    initialTabIndex: Int = 0,
    onLoadInit: () -> Unit,
    onLoadPrevious: (LoadType) -> Unit,
    onNavigateUp: () -> Unit,
    onCardAction: (UserDetailCardAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = 2, initialPage = initialTabIndex)
    val tabTitles = remember {
        listOf(FollowFollowerTabItem(R.string.follow, LoadType.Follow), FollowFollowerTabItem(R.string.follower, LoadType.Follower))
    }
    val scope = rememberCoroutineScope()

    fun onAction(type: LoadType, it: UserDetailCardPageableListAction) {
        when (it) {
            is UserDetailCardPageableListAction.CardAction -> {
                onCardAction(it.cardAction)
            }
            UserDetailCardPageableListAction.OnBottomReached -> {
                onLoadPrevious(type)
            }
            UserDetailCardPageableListAction.Refresh -> {
                onLoadInit()
            }
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth()) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    title = {
                        Text(stringResource(id = if(pagerState.currentPage == 0) R.string.follow else R.string.follower))
                    },
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    tabTitles.forEachIndexed { index, s ->
                        Tab(
                            text = { Text(text = stringResource(id = s.titleRes)) },
                            selected = index == pagerState.currentPage,
                            onClick = {

                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }

                }
            }
        },

    ) {
        HorizontalPager(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            state = pagerState
        ) { pageIndex ->
            val item = tabTitles[pageIndex]
            when(item.type) {
                LoadType.Follow -> {
                    UserDetailCardPageableList(
                        pageableState = uiState.followUsersState,
                        users = uiState.followUsers,
                        isUserNameMain = false,
                        onAction = { action ->
                            onAction(LoadType.Follow, action)
                        }
                    )
                }
                LoadType.Follower -> {
                    UserDetailCardPageableList(
                        pageableState = uiState.followerUsersState,
                        users = uiState.followerUsers,
                        isUserNameMain = false,
                        onAction = { action ->
                            onAction(LoadType.Follower, action)
                        }
                    )
                }
            }

        }
    }
}

data class FollowFollowerTabItem(
    @StringRes val titleRes: Int,
    val type: LoadType
)