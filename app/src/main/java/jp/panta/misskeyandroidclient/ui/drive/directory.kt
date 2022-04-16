package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import net.pantasystem.milktea.data.model.drive.Directory
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.DirectoryViewModel
import androidx.compose.runtime.getValue


import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.panta.misskeyandroidclient.util.compose.isScrolledToTheEnd
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.DriveViewModel
import jp.panta.misskeyandroidclient.ui.drive.viewmodel.DirectoryViewData

@Composable
fun DirectoryListScreen(viewModel: DirectoryViewModel, driveViewModel: DriveViewModel) {
    val list:List<DirectoryViewData> by viewModel.foldersLiveData.observeAsState(emptyList())
    val directories = list.map {
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