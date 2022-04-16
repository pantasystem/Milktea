package jp.panta.misskeyandroidclient.ui.notes.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.data.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.renote.RenotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import androidx.compose.ui.Alignment
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent


@ExperimentalCoroutinesApi
@Composable
fun RenoteUsersScreen(
    renotesViewModel: RenotesViewModel,
    onSelected: (NoteRelation) -> Unit,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter
) {

    val renotes: net.pantasystem.milktea.common.PageableState<List<NoteRelation>> by renotesViewModel.renotes.asLiveData().observeAsState(initial = net.pantasystem.milktea.common.PageableState.Fixed(
        net.pantasystem.milktea.common.StateContent.NotExist()))

    LaunchedEffect(true) {
        renotesViewModel.refresh()
    }

    if(renotes.content is net.pantasystem.milktea.common.StateContent.Exist && (renotes.content as net.pantasystem.milktea.common.StateContent.Exist).rawContent.isNotEmpty()) {
        val content = (renotes.content as net.pantasystem.milktea.common.StateContent.Exist).rawContent
        RenoteUserList(
            notes = content,
            onSelected = onSelected,
            onBottomReached = {
                renotesViewModel.next()
            },
            noteCaptureAPIAdapter = noteCaptureAPIAdapter,
            modifier = Modifier.fillMaxSize()
        )
    }else{
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (renotes) {
                is net.pantasystem.milktea.common.PageableState.Loading -> {
                    CircularProgressIndicator()
                }
                is net.pantasystem.milktea.common.PageableState.Error -> {
                    val error = (renotes as net.pantasystem.milktea.common.PageableState.Error).throwable
                    Text(text = "load error:${error}")
                }
                else -> {
                    Text("renote not exist")
                }
            }
        }

    }
}

@ExperimentalCoroutinesApi
@Composable
fun RenoteUserList(
    notes: List<NoteRelation>,
    onSelected: (NoteRelation)->Unit,
    onBottomReached: ()->Unit,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter?,
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

    LazyColumn(state = scrollState, modifier = modifier) {
        this.items(
            notes.size,
            key = {
                notes[it].note.id
            }
        ) { pos ->
            ItemRenoteUser(
                note = notes[pos],
                onClick = {
                    onSelected.invoke(notes[pos])
                },
                noteCaptureAPIAdapter = noteCaptureAPIAdapter
            )
        }
    }
}