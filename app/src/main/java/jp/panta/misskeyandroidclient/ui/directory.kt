package jp.panta.misskeyandroidclient.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import jp.panta.misskeyandroidclient.model.drive.Directory
import jp.panta.misskeyandroidclient.viewmodel.drive.directory.DirectoryViewModel
import androidx.compose.runtime.getValue


import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DirectoryListView(directories: List<Directory>, onDirectorySelected: (Directory)->Unit) {
    LazyColumn {
        this.itemsIndexed(directories, { index, _ ->
            directories[index].id
        }){ _, item ->
            DirectoryListTile(item) {
                onDirectorySelected.invoke(item)
            }
        }
    }
}