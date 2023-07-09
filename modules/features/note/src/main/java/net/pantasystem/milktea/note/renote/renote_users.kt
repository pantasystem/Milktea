package net.pantasystem.milktea.note.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.renote.ItemRenoteAction
import net.pantasystem.milktea.note.renote.ItemRenoteUser
import net.pantasystem.milktea.note.renote.RenoteItemType
import net.pantasystem.milktea.note.renote.RenotesViewModel


@ExperimentalCoroutinesApi
@Composable
fun RenoteUsersScreen(
    renotesViewModel: RenotesViewModel,
    onSelected: (RenoteItemType) -> Unit,
    onScrollState: (Boolean) -> Unit,
) {

    val myId by renotesViewModel.myId.collectAsState()
    val account by renotesViewModel.account.collectAsState()
    val config by renotesViewModel.config.collectAsState()

    val renotes: PageableState<List<RenoteItemType>> by renotesViewModel.renotes.asLiveData()
        .observeAsState(
            initial = PageableState.Fixed(
                StateContent.NotExist()
            )
        )

    LaunchedEffect(true) {
        renotesViewModel.refresh()
    }

    if (renotes.content is StateContent.Exist && (renotes.content as StateContent.Exist).rawContent.isNotEmpty()) {
        val content = (renotes.content as StateContent.Exist).rawContent
        RenoteUserList(
            notes = content,
            onAction = {
                when(it) {
                    is ItemRenoteAction.OnClick -> {
                        onSelected(it.note)
                    }
                    is ItemRenoteAction.OnDeleteButtonClicked -> {
                        renotesViewModel.delete(it.note)
                    }
                }
            },
            onBottomReached = {
                renotesViewModel.next()
            },
            modifier = Modifier.fillMaxSize(),
            onScrollState = onScrollState,
            myId = myId,
            accountHost = account?.getHost(),
            isDisplayTimestampsAsAbsoluteDates = config.isDisplayTimestampsAsAbsoluteDates,
        )
    } else {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (renotes) {
                is PageableState.Loading -> {
                    CircularProgressIndicator()
                }
                is PageableState.Error -> {
                    val error = (renotes as PageableState.Error).throwable
                    Text(text = "load error:${error}")
                }
                else -> {
                    Text("renote not exist")
                }
            }
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@ExperimentalCoroutinesApi
@Composable
fun RenoteUserList(
    notes: List<RenoteItemType>,
    myId: User.Id?,
    accountHost: String?,
    isDisplayTimestampsAsAbsoluteDates: Boolean?,
    onAction: (ItemRenoteAction) -> Unit,
    onBottomReached: () -> Unit,
    onScrollState: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.mapNotNull { index ->
            index == scrollState.layoutInfo.totalItemsCount - 1
        }.distinctUntilChanged().filter {
            it
        }.collect {
            onBottomReached.invoke()
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset }
            .map { it == 0 }
            .distinctUntilChanged()
            .collect {
                onScrollState.invoke(it)
            }
    }

    LazyColumn(state = scrollState, modifier = modifier.nestedScroll(
        rememberNestedScrollInteropConnection())) {
        this.items(
            notes.size,
        ) { pos ->
            ItemRenoteUser(
                note = notes[pos],
                onAction = onAction,
                myId = myId,
                accountHost = accountHost,
                isDisplayTimestampsAsAbsoluteDates = isDisplayTimestampsAsAbsoluteDates ?: false,
            )
        }
    }
}