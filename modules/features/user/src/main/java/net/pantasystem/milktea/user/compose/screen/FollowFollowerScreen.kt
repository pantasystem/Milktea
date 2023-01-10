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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.compose.UserDetailCardAction
import net.pantasystem.milktea.user.compose.UserDetailCardPageableList
import net.pantasystem.milktea.user.compose.UserDetailCardPageableListAction
import net.pantasystem.milktea.user.viewmodel.FollowFollowerUiState
import net.pantasystem.milktea.user.viewmodel.FollowFollowerViewModel
import net.pantasystem.milktea.user.viewmodel.LoadType
import net.pantasystem.milktea.user.viewmodel.ToggleFollowViewModel

@Composable
fun FollowFollowerRoute(
    initialTabIndex: Int,
    followFollowerViewModel: FollowFollowerViewModel,
    toggleFollowViewModel: ToggleFollowViewModel,
    onCardAction: (UserDetailCardAction) -> Unit,
    onNavigateUp: () -> Unit
) {
    val uiState by followFollowerViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(null) {
        followFollowerViewModel.loadInit()
    }

    val errorMessage = stringResource(id = R.string.failure)
    val retryMessage = stringResource(id = R.string.retry)

    LaunchedEffect(null) {
        toggleFollowViewModel.errors.filterNotNull().collect {
            val result = snackBarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = retryMessage
            )
            when(result) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> {
                    toggleFollowViewModel.toggleFollow(it.userId)
                }
            }
        }
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
        onCardAction = onCardAction,
        snackBarHostState = snackBarHostState
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
    snackBarHostState: SnackbarHostState,
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
        scaffoldState = rememberScaffoldState(snackbarHostState = snackBarHostState),
        topBar = {
            FollowFollowerTopBar(
                modifier = Modifier.fillMaxWidth(),
                uiState = uiState,
                pagerState = pagerState,
                tabTitles = tabTitles,
                onNavigateUp = onNavigateUp,
                scope = scope,
            )
        },

    ) {
        Pager(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            pagerState = pagerState,
            tabTitles = tabTitles,
            uiState = uiState,
            onAction = ::onAction
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun Pager(
    modifier: Modifier,
    pagerState: PagerState,
    tabTitles: List<FollowFollowerTabItem>,
    uiState: FollowFollowerUiState,
    onAction: (type: LoadType, it: UserDetailCardPageableListAction) -> Unit,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState
    ) { pageIndex ->
        val item = tabTitles[pageIndex]
        when (item.type) {
            LoadType.Follow -> {
                UserDetailCardPageableList(
                    pageableState = uiState.followUsersState,
                    users = uiState.followUsers,
                    isUserNameMain = false,
                    accountHost = uiState.accountHost,
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
                    accountHost = uiState.accountHost,
                    onAction = { action ->
                        onAction(LoadType.Follower, action)
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun FollowFollowerTopBar(
    modifier: Modifier,
    uiState: FollowFollowerUiState,
    pagerState: PagerState,
    tabTitles: List<FollowFollowerTabItem>,
    onNavigateUp: () -> Unit,
    scope: CoroutineScope,
) {
    Column(modifier.fillMaxWidth()) {
        TopAppBar(
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.surface,
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            title = {
                if (uiState.user == null) {
                    Text(
                        stringResource(id = if(pagerState.currentPage == 0) R.string.follow else R.string.follower)
                    )
                } else {
                    CustomEmojiText(
                        text = uiState.user.displayName,
                        emojis = uiState.user.emojis,
                        sourceHost = uiState.user.host,
                        accountHost = uiState.accountHost,
                        parsedResult = uiState.user.parsedResult,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )
                }

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
}

data class FollowFollowerTabItem(
    @StringRes val titleRes: Int,
    val type: LoadType
)