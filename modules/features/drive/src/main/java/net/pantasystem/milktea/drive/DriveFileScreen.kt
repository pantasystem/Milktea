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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import net.pantasystem.milktea.model.drive.FileProperty

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun FilePropertyListScreen(
    driveViewModel: DriveViewModel,
    isGridMode: Boolean,
) {
    val uiState by driveViewModel.uiState.collectAsState()
    val filesState = uiState.driveFilesState

    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = filesState is PageableState.Loading.Init || filesState is PageableState.Loading.Future
    )
    val isSelectMode: Boolean = uiState.isSelectMode
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
            driveViewModel.deleteFile(confirmDeleteTarget!!.id)
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
            driveViewModel.updateCaption(id, newCaption)
        }
    )

    EditFileNameDialog(
        fileProperty = editNameTargetFile,
        onDismiss = { editNameTargetFile = null },
        onSave = { id, newName ->
            editNameTargetFile = null
            driveViewModel.updateFileName(id, newName)
        },
    )

    val actionHandler: (FilePropertyCardAction) -> Unit = { cardAction ->
        when (cardAction) {
            is FilePropertyCardAction.OnCloseDropdownMenu -> {
                driveViewModel.closeFileCardDropDownMenu()
            }
            is FilePropertyCardAction.OnOpenDropdownMenu -> {
                driveViewModel.openFileCardDropDownMenu(cardAction.fileId)
            }
            is FilePropertyCardAction.OnToggleSelectItem -> {
                driveViewModel.toggleSelect(cardAction.fileId)
            }
            is FilePropertyCardAction.OnToggleNsfw -> {
                driveViewModel.toggleNsfw(cardAction.fileId)
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
            driveViewModel.onFileListRefreshed()
        }
    ) {
        if (isGridMode) {
            DriveFilesGridView(
                files = files,
                onLoadNext = {
                    driveViewModel.onFileListViewBottomReached()
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
                    driveViewModel.onFileListViewBottomReached()
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