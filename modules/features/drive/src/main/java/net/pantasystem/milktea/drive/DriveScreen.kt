package net.pantasystem.milktea.drive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import net.pantasystem.milktea.drive.viewmodel.DriveViewModel
import net.pantasystem.milktea.drive.viewmodel.FileViewModel
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty


@ExperimentalPagerApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun DriveScreen(
    driveViewModel: DriveViewModel,
    fileViewModel: FileViewModel,
    onNavigateUp: () -> Unit,
    onFixSelected: () -> Unit,
    onShowLocalFilePicker: () -> Unit,
    onShowCreateDirectoryEditor: () -> Unit,
    tabTitles: List<String> = listOf(
        stringResource(id = R.string.drive_file),
        stringResource(id = R.string.drive_folder)
    )
) {
    require(tabTitles.size == 2)

    val isGridMode: Boolean by driveViewModel.isUsingGridView.collectAsState()

    val isSelectMode: Boolean by driveViewModel.isSelectMode.collectAsState()

//    val selectableMaxCount = driveViewModel.selectable?.selectableMaxSize
    val selectedFileIds: Set<FileProperty.Id>? by fileViewModel.selectedFileIds.asLiveData()
        .observeAsState(initial = emptySet())
//    val path: List<PathViewData> by driveViewModel.path.asLiveData()
//        .observeAsState(initial = emptyList())
    val selectableMaxCount = driveViewModel.maxSelectableSize.collectAsState()

    val uiState by driveViewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = tabTitles.size)
    val scope = rememberCoroutineScope()



    Scaffold(
        topBar = {

            Column {

                TopAppBar(
                    title = {
                        if (isSelectMode) {
                            Text("${stringResource(R.string.selected)} ${selectedFileIds?.size ?: 0}/${selectableMaxCount}")
                        } else {
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
                        if (isSelectMode) {
                            IconButton(onClick = onFixSelected) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Fix")
                            }
                        }
                    },
                    elevation = 0.dp,
                    backgroundColor = MaterialTheme.colors.surface

                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PathHorizontalView(currentDir = uiState.currentDirectory, modifier = Modifier.weight(1f)) { dir ->
                        driveViewModel.popUntil(dir)
                    }
                    ToggleViewMode(isGridMode = isGridMode) {
                        driveViewModel.setUsingGridView(!isGridMode)
                    }
                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    tabTitles.forEachIndexed { index, s ->
                        Tab(
                            text = { Text(text = s) },
                            selected = index == pagerState.currentPage,
                            onClick = {

                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }

                            }

                        )
                    }

                }
            }
        },
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                FloatingActionButton(onClick = onShowLocalFilePicker) {
                    Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = null)
                }
            } else {
                FloatingActionButton(onClick = onShowCreateDirectoryEditor) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            }

        }

    ) { padding ->


        HorizontalPager(state = pagerState, modifier = Modifier.padding(padding)) { page ->
            if (page == 0) {
                FilePropertyListScreen(
                    driveViewModel = driveViewModel,
                    isGridMode = isGridMode
                )
            } else {
                DirectoryListScreen(driveViewModel = driveViewModel)
            }
        }
    }
}


@Composable
fun PathHorizontalView(
    modifier: Modifier = Modifier,
    currentDir: Directory?,
    onSelected: (Directory?) -> Unit
) {

    val path = remember(currentDir) {
        val path = mutableListOf<Directory>()
        var dir = currentDir
        while (dir != null) {
            path.add(0, dir)
            dir = dir.parent
        }
        path
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.surface,
    ) {
        LazyRow(
            Modifier
                .fillMaxWidth(),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            onSelected.invoke(null)
                        }

                ) {
                    Text(text = "root")
                    Icon(imageVector = Icons.Filled.ArrowRight, contentDescription = null)

                }
            }


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

}

@Composable
private fun ToggleViewMode(
    modifier: Modifier = Modifier,
    isGridMode: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clickable {
                onClick()
            }
            .clip(RoundedCornerShape(4.dp))
    ) {
        if (isGridMode) {
            Icon(Icons.Default.Grid3x3, contentDescription = null, modifier = Modifier.size(24.dp))
        } else {
            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}