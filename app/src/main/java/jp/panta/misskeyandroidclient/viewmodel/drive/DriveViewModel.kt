package jp.panta.misskeyandroidclient.viewmodel.drive

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class DriveSelectableMode(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: List<FileProperty.Id>,
    val accountId: Long
)
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

    val currentDirectory = this.driveStore.state.map {
        if(it.path.path.isEmpty()) {
            PathViewData(null)
        }else{
            it.path.path.last()
        }
    }

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

    val openFileEvent = EventBus<FileProperty>()
    //val selectedFilesMap = HashMap<String, FileViewData>()


    private val _selectedFileIds = MutableLiveData<Set<FileProperty.Id>>()

    fun getSelectedFileIds(): Set<FileProperty.Id>?{
        return this.driveStore.state.value.selectedFilePropertyIds?.selectedIds
    }

    fun setSelectedFileIds(fileIds: List<FileProperty.Id>) {
        _selectedFileIds.postValue(fileIds.toSet())
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

    fun popUntil(directory: Directory) {
        driveStore.popUntil(directory)
    }



    fun openFile(fileProperty: FileProperty){
        openFileEvent.event = fileProperty
    }


}