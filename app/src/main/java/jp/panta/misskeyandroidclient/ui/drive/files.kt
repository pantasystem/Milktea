package jp.panta.misskeyandroidclient.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.util.PageableState
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
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
            state = listViewState
        )
    }
}
@Composable
fun FileViewDataListView(
    list: List<FileViewData>,
    isSelectMode: Boolean = false,
    onCheckedChanged: (FileProperty.Id, Boolean) -> Unit,
    state: LazyListState = rememberLazyListState(),
) {
    LazyColumn(state = state) {
        this.items(
            list,
            key = {
                it.fileProperty.id
            }
        ) { item ->
            FilePropertySimpleCard(file = item, isSelectMode = isSelectMode, onCheckedChanged = {
                onCheckedChanged.invoke(item.fileProperty.id, it)
            })
        }
    }
}

@Composable
fun FilePropertySimpleCard(file: FileViewData, isSelectMode: Boolean = false, onCheckedChanged: (Boolean)->Unit ) {
    Card(
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                painter = rememberGlidePainter(
                    request = file.fileProperty.url,
                ),
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .width(50.dp)
            )
            Column {
                Text(
                    file.fileProperty.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
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
            if(isSelectMode) {
                Checkbox(checked = file.isSelected, enabled = file.isEnabled, onCheckedChange = onCheckedChanged)
            }

        }
    }
}