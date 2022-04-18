package jp.panta.misskeyandroidclient.ui.drive.viewmodel

import androidx.lifecycle.ViewModel
import net.pantasystem.milktea.data.model.drive.*
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

data class DriveSelectableMode(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: List<net.pantasystem.milktea.model.drive.FileProperty.Id>,
    val accountId: Long
) : Serializable
class DriveViewModel(
    val selectable: DriveSelectableMode?
) : ViewModel(){

    val driveStore: net.pantasystem.milktea.model.drive.DriveStore =
        net.pantasystem.milktea.model.drive.DriveStore(
            net.pantasystem.milktea.model.drive.DriveState(
                accountId = selectable?.accountId,
                path = net.pantasystem.milktea.model.drive.DirectoryPath(emptyList()),
                selectedFilePropertyIds = selectable?.let {
                    net.pantasystem.milktea.model.drive.SelectedFilePropertyIds(
                        selectableMaxCount = it.selectableMaxSize,
                        selectedIds = it.selectedFilePropertyIds.toSet()
                    )
                }
            )
        )



    val path: Flow<List<PathViewData>> = driveStore.state.map { state ->
        mutableListOf(
            PathViewData(null),
        ).also { list ->
            list.addAll(
                state.path.path.map { directory ->
                    PathViewData(directory)
                }
            )
        }
    }


    val isSelectMode = driveStore.state.map {
        it.isSelectMode
    }



    fun getSelectedFileIds(): Set<net.pantasystem.milktea.model.drive.FileProperty.Id>?{
        return this.driveStore.state.value.selectedFilePropertyIds?.selectedIds
    }



    fun push(directory: net.pantasystem.milktea.model.drive.Directory) {
        this.driveStore.push(directory)
    }


    fun pop() : Boolean{
        val path = driveStore.state.value.path.path
        if(path.isEmpty()) {
            return false
        }

        return driveStore.pop()
    }

    fun popUntil(directory: net.pantasystem.milktea.model.drive.Directory?) {
        driveStore.popUntil(directory)
    }


}