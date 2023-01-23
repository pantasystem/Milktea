package net.pantasystem.milktea.userlist.compose

import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.userlist.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun UserListDetailScreen(
    listId: UserList.Id,
    userList: UserList?,
    users: List<User>,
    accountHost: String?,
    isAddedTab: Boolean,
    onNavigateUp: () -> Unit,
    fragmentManager: FragmentManager,
    pageableFragmentFactory: PageableFragmentFactory,
    onToggleButtonClicked: () -> Unit,
    onEditButtonClicked: () -> Unit,
    onAddUserButtonClicked: () -> Unit,
    onSelectUser: (User) -> Unit,
    onDeleteUserButtonClicked: (User) -> Unit,
) {
//    val userList by mUserListDetailViewModel.userList.collectAsState()
    val titles =
        listOf(stringResource(R.string.timeline), stringResource(R.string.user_list))
    val pagerState = rememberPagerState(pageCount = titles.size)
////    val users by mUserListDetailViewModel.users.collectAsState()
//    val isAddedTab by mUserListDetailViewModel.isAddedToTab.collectAsState()

    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    },
                    title = {
                        Text(userList?.name ?: "")
                    },
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    actions = {
                        IconButton(onClick = onAddUserButtonClicked) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                        }

                        IconButton(onClick = onEditButtonClicked) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                        IconButton(onClick = onToggleButtonClicked) {
                            if (isAddedTab) {
                                Icon(
                                    Icons.Default.BookmarkRemove,
                                    contentDescription = null
                                )
                            } else {
                                Icon(
                                    Icons.Default.BookmarkAdd,
                                    contentDescription = null
                                )
                            }
                        }

                    }

                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = MaterialTheme.colors.surface,
                ) {
                    titles.forEachIndexed { index, s ->
                        Tab(
                            text = { Text(text = s) },
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
    ) { paddingValues ->
        HorizontalPager(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            state = pagerState,
        ) {
            when (pagerState.currentPage) {
                0 -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            FrameLayout(context).apply {
                                id = R.id.container
                            }
                        },
                        update = { frameLayout ->
                            val fragment = pageableFragmentFactory.create(
                                Pageable.UserListTimeline(listId = listId.userListId)
                            )
                            val transaction = fragmentManager.beginTransaction()
                            transaction.replace(frameLayout.id, fragment)
                            transaction.commit()
                        }
                    )
                }
                1 -> {
                    RemovableSimpleUserCardList(
                        modifier = Modifier.fillMaxSize(),
                        users = users,
                        onSelectUser = onSelectUser,
                        onDeleteButtonClicked = onDeleteUserButtonClicked,
                        accountHost = accountHost,
                    )

                }
            }
        }
    }
}