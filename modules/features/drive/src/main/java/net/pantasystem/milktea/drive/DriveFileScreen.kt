package net.pantasystem.milktea.drive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
fun FilePropertyListScreen(
    fileViewModel: FileViewModel,
    driveViewModel: DriveViewModel,
    isGridMode: Boolean,
) {
    val filesState: PageableState<List<FileViewData>> by fileViewModel.state.collectAsState()

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = filesState is PageableState.Loading.Init || filesState is PageableState.Loading.Future
    )
    val isSelectMode: Boolean by driveViewModel.isSelectMode.asLiveData()
        .observeAsState(initial = false)
    val files = (filesState.content as? StateContent.Exist)?.rawContent ?: emptyList()

    var confirmDeleteTarget: FileProperty? by remember {
        mutableStateOf(null)
    }


    var editCaptionTargetFile: FileProperty? by remember {
        mutableStateOf(null)
    }

    var editNameTargetFile: FileProperty? by remember {
        mutableStateOf(null)
    }

    ConfirmDeleteFilePropertyDialog(
        isShow = confirmDeleteTarget != null,
        filename = confirmDeleteTarget?.name ?: "",
        onDismissRequest = {
            confirmDeleteTarget = null
        },
        onConfirmed = {
            fileViewModel.deleteFile(confirmDeleteTarget!!.id)
            confirmDeleteTarget = null
        }
    )

    EditCaptionDialog(
        fileProperty = editCaptionTargetFile,
        onDismiss = {
            editCaptionTargetFile = null
        },
        onSave = { id, newCaption ->
            editCaptionTargetFile = null
            fileViewModel.updateCaption(id, newCaption)
        }
    )

    EditFileNameDialog(
        fileProperty = editNameTargetFile,
        onDismiss = { editNameTargetFile = null },
        onSave = { id, newName ->
            editNameTargetFile = null
            fileViewModel.updateFileName(id, newName)
        },
    )

    val actionHandler: (FilePropertyCardAction) -> Unit = { cardAction ->
        when (cardAction) {
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
            is FilePropertyCardAction.OnSelectEditCaptionMenuItem -> {
                editCaptionTargetFile = cardAction.file
            }
            is FilePropertyCardAction.OnSelectEditFileNameMenuItem -> {
                editNameTargetFile = cardAction.file
            }
        }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            fileViewModel.loadInit()
        }
    ) {
        if (isGridMode) {
            DriveFilesGridView(
                files = files,
                onLoadNext = {
                    fileViewModel.loadNext()
                },
                isSelectMode = isSelectMode,
                onAction = actionHandler
            )
        } else {
            FileViewDataListView(
                files,
                isSelectMode,
                onAction = actionHandler,
                onLoadNext = {
                    fileViewModel.loadNext()
                }
            )
        }

    }
}

@ExperimentalMaterialApi
@Composable
fun FileViewDataListView(
    list: List<FileViewData>,
    isSelectMode: Boolean = false,
    onLoadNext: () -> Unit,
    onAction: (FilePropertyCardAction) -> Unit,
) {
    val state: LazyListState = rememberLazyListState()

    LaunchedEffect(null) {
        snapshotFlow {
            state.isScrolledToTheEnd() && state.layoutInfo.totalItemsCount != state.layoutInfo.visibleItemsInfo.size && state.isScrollInProgress
        }.distinctUntilChanged().onEach {
            if (it) {
                onLoadNext()
            }
        }.launchIn(this)
    }
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
                onAction = onAction,
            )
        }
    }
}


@Composable
fun DriveFilesGridView(
    files: List<FileViewData>,
    isSelectMode: Boolean = false,
    onLoadNext: () -> Unit,
    onAction: (FilePropertyCardAction) -> Unit,
) {
    val state = rememberLazyGridState()
    LaunchedEffect(null) {
        snapshotFlow {
            state.isScrolledToTheEnd() && state.layoutInfo.totalItemsCount != state.layoutInfo.visibleItemsInfo.size && state.isScrollInProgress
        }.distinctUntilChanged().onEach {
            if (it) {
                onLoadNext()
            }
        }.launchIn(this)
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.fillMaxSize(),
        state = state
    ) {
        items(files.size) { index ->
            FilePropertyGridItem(
                fileViewData = files[index],
                onAction = onAction,
                isSelectMode = isSelectMode
            )
        }
    }
}