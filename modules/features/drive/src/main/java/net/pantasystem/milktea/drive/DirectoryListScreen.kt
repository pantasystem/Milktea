package net.pantasystem.milktea.drive


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.model.drive.Directory

@Composable
fun DirectoryListScreen(driveViewModel: DriveViewModel) {
    val uiState by driveViewModel.uiState.collectAsState()
    val state: PageableState<List<Directory>> = uiState.directoriesState

    val directories = ((state.content as? StateContent.Exist)?.rawContent?: emptyList())
    val isLoading: Boolean = uiState.directoriesState is PageableState.Loading.Init
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    val listState = rememberLazyListState()


    if(listState.isScrolledToTheEnd() && listState.layoutInfo.visibleItemsInfo.size !=  listState.layoutInfo.totalItemsCount && listState.isScrollInProgress){
        driveViewModel.onDirectoryListViewBottomReached()
    }
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            driveViewModel.onDirectoryListRefreshed()
        },
        Modifier.fillMaxHeight()
    ) {
        DirectoryListView(
            uiState.canFileMove,
            directories,
            listState = listState,
            onDirectorySelected = {
                driveViewModel.push(it)
            },
            onMoveToFileHereButtonClicked = driveViewModel::onFileMoveToHereButtonClicked
        )
    }



}

@Composable
fun DirectoryListTile(directory: Directory, onClick:()->Unit) {
    Card (
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp)
    ){
        Column(
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                directory.name,
                fontSize = 20.sp
            )
        }
    }

}

@Composable
fun DirectoryListView(
    canFileMove: Boolean,
    directories: List<Directory>,
    listState: LazyListState = rememberLazyListState(),
    onDirectorySelected: (Directory)->Unit,
    onMoveToFileHereButtonClicked: ()->Unit = {}
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        if (canFileMove) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = onMoveToFileHereButtonClicked,
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 32.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Text("ファイルをここに移動")
                    }
                }
            }
        }
        this.itemsIndexed(directories, { index, _ ->
            directories[index].id
        }){ _, item ->
            DirectoryListTile(item) {
                onDirectorySelected.invoke(item)
            }
        }
    }
}