package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.util.compose.isScrolledToTheEnd
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun FilePropertyListScreen(fileViewModel: FileViewModel, driveViewModel: DriveViewModel) {
    val filesState: PageableState<List<FileViewData>> by fileViewModel.state.asLiveData().observeAsState(
        initial = PageableState.Fixed(StateContent.NotExist())
    )
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = filesState is PageableState.Loading.Init || filesState is PageableState.Loading.Future
    )
    val isSelectMode: Boolean by driveViewModel.isSelectMode.asLiveData().observeAsState(initial = false)
    val files = (filesState.content as? StateContent.Exist)?.rawContent ?: emptyList()
    val listViewState = rememberLazyListState()
    if(listViewState.isScrolledToTheEnd() && listViewState.layoutInfo.totalItemsCount != listViewState.layoutInfo.visibleItemsInfo.size && listViewState.isScrollInProgress) {
        fileViewModel.loadNext()
    }
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh =  {
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
            )
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun FilePropertySimpleCard(
    file: FileViewData,
    isSelectMode: Boolean = false,
    onCheckedChanged: (Boolean)->Unit,
    onDeleteMenuItemClicked: () -> Unit,
    onToggleNsfwMenuItemClicked: () -> Unit,
) {
    var actionMenuExpandedState by remember {
        mutableStateOf(false)
    }

    var confirmDeleteTargetId by remember {
        mutableStateOf<FileProperty.Id?>(null)
    }


    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = if(file.isSelected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        },
        onClick = {
            if(isSelectMode) {
                onCheckedChanged.invoke(!file.isSelected)
            }else{
                actionMenuExpandedState = true
            }

        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberImagePainter(
                        file.fileProperty.thumbnailUrl
                            ?: file.fileProperty.url
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .height(64.dp)
                        .width(64.dp)
                        .padding(end = 4.dp),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        file.fileProperty.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Row {
                        Text(
                            file.fileProperty.type,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            file.fileProperty.size.toString()
                        )
                    }
                }


            }
            Box(
                modifier = Modifier.align(Alignment.End)
            ){
                FileActionDropdownMenu(

                    expanded = actionMenuExpandedState,
                    onDismissRequest = {
                        actionMenuExpandedState = false
                    },
                    onNsfwMenuItemClicked = onToggleNsfwMenuItemClicked,
                    onDeleteMenuItemClicked = {
                        actionMenuExpandedState = false
                        confirmDeleteTargetId = file.fileProperty.id
                    },
                    property = file.fileProperty
                )
            }

        }


    }
    if(confirmDeleteTargetId != null) {
        ConfirmDeleteFilePropertyDialog(
            filename = file.fileProperty.name,
            onDismissRequest = {
                confirmDeleteTargetId = null
            },
            onConfirmed = {
                confirmDeleteTargetId = null
                onDeleteMenuItemClicked()
            }
        )
    }

}

