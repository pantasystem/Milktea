package net.pantasystem.milktea.drive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.drive.viewmodel.FileViewData
import net.pantasystem.milktea.drive.viewmodel.FileViewModel
import net.pantasystem.milktea.model.drive.FileProperty

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun FilePropertyListScreen(fileViewModel: FileViewModel, driveViewModel: DriveViewModel) {
    val filesState: PageableState<List<FileViewData>> by fileViewModel.state.collectAsState()

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
    var confirmDeleteTarget: FileProperty? by remember {
        mutableStateOf(null)
    }

    if (confirmDeleteTarget != null) {
        ConfirmDeleteFilePropertyDialog(
            filename = confirmDeleteTarget!!.name,
            onDismissRequest = {
                confirmDeleteTarget = null
            },
            onConfirmed = {
                fileViewModel.deleteFile(confirmDeleteTarget!!.id)
                confirmDeleteTarget = null
            }
        )
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
            state = listViewState,
            onEditFileCaption = { id, newCaption ->
                fileViewModel.updateCaption(id, newCaption)
            },
            onAction = { cardAction ->
                when(cardAction) {
                    is FilePropertyCardAction.OnCloseDropdownMenu -> {
                        fileViewModel.closeFileCardDropDownMenu()
                    }
                    is FilePropertyCardAction.OnOpenDropdownMenu -> {
                        fileViewModel.openFileCardDropDownMenu(cardAction.fileId)
                    }
                    is FilePropertyCardAction.OnToggleSelectItem -> {
                        driveViewModel.driveStore.toggleSelect(cardAction.fileId)
                    }
                    is FilePropertyCardAction.OnToggleNsfw -> {
                        fileViewModel.toggleNsfw(cardAction.fileId)
                    }
                    is FilePropertyCardAction.OnSelectDeletionMenuItem -> {
                        confirmDeleteTarget = cardAction.file
                    }
                }
            }
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun FileViewDataListView(
    list: List<FileViewData>,
    isSelectMode: Boolean = false,
    onEditFileCaption: (FileProperty.Id, String) -> Unit,
    state: LazyListState = rememberLazyListState(),
    onAction: (FilePropertyCardAction) -> Unit,
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
                onEditFileCaption = onEditFileCaption,
                onAction = onAction,
            )
        }
    }
}
