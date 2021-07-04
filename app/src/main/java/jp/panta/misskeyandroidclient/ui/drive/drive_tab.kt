package jp.panta.misskeyandroidclient.ui.drive

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.file.FileViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.DirectoryListScreen

import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@Composable
fun DriveTabScreen (
    fileViewModel: FileViewModel,
    driveViewModel: DriveViewModel,
    directoryViewModel: DirectoryViewModel
) {


    var currentTabIndex: Int by remember {
        mutableStateOf(0)
    }
    val tabTitles = listOf(
        stringResource(id = R.string.file),
        stringResource(id = R.string.folder)
    )


    Column {

        TabRow(
            selectedTabIndex = currentTabIndex,
        ) {
            Tab(selected = 0 == currentTabIndex, onClick = { currentTabIndex = 0}) {
                Text(tabTitles[0])
            }
            Tab(selected = 1 == currentTabIndex, onClick = { currentTabIndex = 1}) {
                Text(tabTitles[1])
            }
        }
        if(currentTabIndex == 0) {
            FilePropertyListScreen(fileViewModel = fileViewModel, driveViewModel = driveViewModel)
        }else{
            DirectoryListScreen(viewModel = directoryViewModel, driveViewModel = driveViewModel)
        }
    }

}
