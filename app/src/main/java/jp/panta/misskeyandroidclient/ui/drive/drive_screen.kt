package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import com.google.accompanist.pager.ExperimentalPagerApi
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.PathViewData
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalPagerApi
@ExperimentalCoroutinesApi
@Composable
fun DriveScreen(
    driveViewModel: DriveViewModel,
    fileViewModel: FileViewModel,
    directoryViewModel: DirectoryViewModel,
    onNavigateUp: ()->Unit,
    onFixSelected: ()->Unit,
    onShowLocalFilePicker: ()->Unit,
    onShowCreateDirectoryEditor: ()-> Unit,
    tabTitles: List<String> = listOf(
        stringResource(id = R.string.file),
        stringResource(id = R.string.folder)
    )
) {
    require(tabTitles.size == 2)

    val isSelectMode: Boolean by  driveViewModel.isSelectMode.asLiveData().observeAsState(initial = false)
    val selectableMaxCount = driveViewModel.selectable?.selectableMaxSize
    val selectedFileIds: Set<FileProperty.Id>? by fileViewModel.selectedFileIds.asLiveData().observeAsState(initial = emptySet())
    val path: List<PathViewData> by driveViewModel.path.asLiveData().observeAsState(initial = emptyList())

    //val pagerState = rememberPagerState(pageCount = tabTitles.size)
    var currentPageIndex: Int by remember {
        mutableStateOf(0)
    }

    //val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {

            Column {

                TopAppBar (
                    title = {
                        if(isSelectMode) {
                            Text("${stringResource(R.string.selected)} ${selectedFileIds?.size?: 0}/${selectableMaxCount}")
                        }else{
                            Text(stringResource(id = R.string.drive))
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp.invoke()
                            },
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if(isSelectMode) {
                            IconButton(onClick = onFixSelected) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Fix")
                            }
                        }
                    },
                    elevation = 0.dp

                )
                PathHorizontalView(path = path) { dir ->
                    driveViewModel.popUntil(dir.folder)
                }

                TabRow(selectedTabIndex = currentPageIndex) {
                    tabTitles.forEachIndexed { index, s ->
                        Tab(
                            text = {  Text(text = s) },
                            selected = index == currentPageIndex,
                            onClick = {
                                /*scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }*/
                                currentPageIndex = index

                            }

                        )
                    }

                }
            }
        },
        floatingActionButton = {
            if(currentPageIndex == 0) {
                FloatingActionButton(onClick = onShowLocalFilePicker) {
                    Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = null)
                }
            }else{
                FloatingActionButton(onClick = onShowCreateDirectoryEditor) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            }

        }

    ) {

        if(currentPageIndex == 0) {
            FilePropertyListScreen(fileViewModel = fileViewModel, driveViewModel = driveViewModel)
        }else{
            DirectoryListScreen(viewModel = directoryViewModel, driveViewModel = driveViewModel)
        }
        /*
        FIXME: HorizontalPagerを使用するとSwipeRefresh時にクラッシュする
        HorizontalPager(state = pagerState) { page ->
            if(page == 0) {
                FilePropertyListScreen(fileViewModel = fileViewModel, driveViewModel = driveViewModel)
            }else{
                DirectoryListScreen(viewModel = directoryViewModel, driveViewModel = driveViewModel)
            }
        }
         */
    }
}



@Composable
fun PathHorizontalView(path: List<PathViewData>, onSelected: (PathViewData)->Unit) {
    LazyRow(
        Modifier
            .background(MaterialTheme.colors.primary)
            .fillMaxWidth(),

    ){
        this.items(path, key = {
            it.id to it.name
        }) { dir ->
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable {
                        onSelected.invoke(dir)
                    }

            ) {
                Text(text = dir.name)
                Icon(imageVector = Icons.Filled.ArrowRight, contentDescription = null)

            }
        }
    }

}