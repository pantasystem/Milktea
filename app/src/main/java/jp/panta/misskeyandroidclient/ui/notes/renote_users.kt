package jp.panta.misskeyandroidclient.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.viewmodel.notes.renote.RenotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.getValue
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent


@ExperimentalCoroutinesApi
@Composable
fun RenoteUsersScreen(
    renotesViewModel: RenotesViewModel,
    onSelected: (NoteRelation) -> Unit,
    noteCaptureAPIAdapter: NoteCaptureAPIAdapter
) {
    val renotes: PageableState<List<NoteRelation>> by renotesViewModel.renotes.asLiveData().observeAsState(initial = PageableState.Fixed(StateContent.NotExist()))

    if(renotes.content is StateContent.Exist) {
        val content = (renotes.content as StateContent.Exist).rawContent
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
        Column(Modifier.fillMaxSize()) {
            when (renotes) {
                is PageableState.Loading -> {
                    CircularProgressIndicator()
                }
                is PageableState.Error -> {

                }
                else -> {

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