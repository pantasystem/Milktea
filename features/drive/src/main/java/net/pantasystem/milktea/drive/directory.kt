package net.pantasystem.milktea.drive


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.drive.viewmodel.DirectoryViewData
import net.pantasystem.milktea.drive.viewmodel.DirectoryViewModel
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.model.drive.Directory

@Composable
fun DirectoryListScreen(viewModel: DirectoryViewModel, driveViewModel: DriveViewModel) {
    val state: PageableState<List<DirectoryViewData>> by viewModel.foldersLiveData.collectAsState()

    val directories = ((state.content as? StateContent.Exist)?.rawContent?: emptyList()).map {
        it.directory
    }
    val isLoading: Boolean by viewModel.isRefreshing.observeAsState(
        initial = false
    )
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    val listState = rememberLazyListState()


    if(listState.isScrolledToTheEnd() && listState.layoutInfo.visibleItemsInfo.size !=  listState.layoutInfo.totalItemsCount && listState.isScrollInProgress){
        viewModel.loadNext()
    }
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.loadInit()
        },
        Modifier.fillMaxHeight()
    ) {
        DirectoryListView(directories, listState = listState) {
            driveViewModel.push(it)
        }
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
    directories: List<Directory>,
    listState: LazyListState = rememberLazyListState(),
    onDirectorySelected: (Directory)->Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        this.itemsIndexed(directories, { index, _ ->
            directories[index].id
        }){ _, item ->
            DirectoryListTile(item) {
                onDirectorySelected.invoke(item)
            }
        }
    }
}