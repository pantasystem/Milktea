package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.DriveViewModel
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.file.FileViewData
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.file.FileViewModel
import jp.panta.misskeyandroidclient.util.compose.isScrolledToTheEnd
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.drive.FileProperty

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun FilePropertyListScreen(fileViewModel: FileViewModel, driveViewModel: DriveViewModel) {
    val filesState: PageableState<List<FileViewData>> by fileViewModel.state.asLiveData()
        .observeAsState(
            initial = PageableState.Fixed(StateContent.NotExist())
        )
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = filesState is PageableState.Loading.Init || filesState is PageableState.Loading.Future
    )
    val isSelectMode: Boolean by driveViewModel.isSelectMode.asLiveData()
        .observeAsState(initial = false)
    val files = (filesState.content as? StateContent.Exist)?.rawContent ?: emptyList()
    val listViewState = rememberLazyListState()
    LaunchedEffect(null) {
        snapshotFlow {
            listViewState.isScrolledToTheEnd() && listViewState.layoutInfo.totalItemsCount != listViewState.layoutInfo.visibleItemsInfo.size && listViewState.isScrollInProgress
        }.distinctUntilChanged().onEach {
            if (it) {
                fileViewModel.loadNext()
            }
        }.launchIn(this)
    }
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            fileViewModel.loadInit()
        }
    ) {
        FileViewDataListView(
            files,
            isSelectMode,
            onCheckedChanged = { id, _ ->
                driveViewModel.driveStore.toggleSelect(id)
            },
            state = listViewState,
            onToggleNsfwMenuItemClicked = {
                fileViewModel.toggleNsfw(it)
            },
            onDeleteMenuItemClicked = {
                fileViewModel.deleteFile(it)
            },
            onEditFileCaption = { id, newCaption ->
                fileViewModel.updateCaption(id, newCaption)
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun FileViewDataListView(
    list: List<FileViewData>,
    isSelectMode: Boolean = false,
    onCheckedChanged: (FileProperty.Id, Boolean) -> Unit,
    onDeleteMenuItemClicked: (FileProperty.Id) -> Unit,
    onToggleNsfwMenuItemClicked: (FileProperty.Id) -> Unit,
    onEditFileCaption: (FileProperty.Id, String) -> Unit,
    state: LazyListState = rememberLazyListState(),
) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        this.items(
            list,
            key = {
                it.fileProperty.id
            }
        ) { item ->
            FilePropertySimpleCard(
                file = item,
                isSelectMode = isSelectMode,
                onCheckedChanged = {
                    onCheckedChanged.invoke(item.fileProperty.id, it)
                },
                onDeleteMenuItemClicked = { onDeleteMenuItemClicked(item.fileProperty.id) },
                onToggleNsfwMenuItemClicked = { onToggleNsfwMenuItemClicked(item.fileProperty.id) },
                onEditFileCaption = onEditFileCaption
            )
        }
    }
}
