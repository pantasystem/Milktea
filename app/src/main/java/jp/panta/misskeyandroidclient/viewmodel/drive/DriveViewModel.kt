package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.drive.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable

data class DriveSelectableMode(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: List<FileProperty.Id>,
    val accountId: Long
) : Serializable
class DriveViewModel(
    val selectable: DriveSelectableMode?
) : ViewModel(){

    val driveStore: DriveStore = DriveStore(
        DriveState(
            accountId = selectable?.accountId,
            path = DirectoryPath(emptyList()),
            selectedFilePropertyIds = selectable?.let {
                SelectedFilePropertyIds(
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



    fun getSelectedFileIds(): Set<FileProperty.Id>?{
        return this.driveStore.state.value.selectedFilePropertyIds?.selectedIds
    }



    fun push(directory: Directory) {
        this.driveStore.push(directory)
    }


    fun pop() : Boolean{
        val path = driveStore.state.value.path.path
        if(path.isEmpty()) {
            return false
        }

        return driveStore.pop()
    }

    fun popUntil(directory: Directory?) {
        driveStore.popUntil(directory)
    }


}