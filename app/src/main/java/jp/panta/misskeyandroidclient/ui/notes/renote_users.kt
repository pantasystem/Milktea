package jp.panta.misskeyandroidclient.ui.notes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

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