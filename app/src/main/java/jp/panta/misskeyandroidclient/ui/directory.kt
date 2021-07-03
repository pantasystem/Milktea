package jp.panta.misskeyandroidclient.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import jp.panta.misskeyandroidclient.model.drive.Directory
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import androidx.compose.runtime.getValue


import androidx.compose.runtime.livedata.observeAsState
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveViewModel
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewData

@Composable
fun DirectoryListScreen(viewModel: DirectoryViewModel, driveViewModel: DriveViewModel) {
    val list:List<DirectoryViewData> by viewModel.foldersLiveData.observeAsState(emptyList())
    val directories = list.map {
        it.directory
    }

    DirectoryListView(directories) {
        driveViewModel.push(it)
    }


}

@Composable
fun DirectoryListTile(directory: Directory, onClick:()->Unit) {
    Column {
        Text(
            "hogehoeg"
        )
    }
}

@Composable
fun DirectoryListView(directories: List<Directory>, onDirectorySelected: (Directory)->Unit) {
    LazyColumn {
        this.items(directories, {
            it.id
        }){
            DirectoryListTile(it) {
                onDirectorySelected.invoke(it)
            }
        }
    }
}