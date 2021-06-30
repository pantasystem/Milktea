package jp.panta.misskeyandroidclient.model.drive

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


data class DriveState (
    val path: DirectoryPath,
    val accountId: Long,
    val selectedFilePropertyIds: SelectedFilePropertyIds?
) {
    val isSelectMode: Boolean get() = selectedFilePropertyIds != null
}

class DriveStore(
    iniState: DriveState
) {

    private val _state = MutableStateFlow(iniState)
    val state: StateFlow<DriveState> get() = _state



    fun toggleSelect(id: FileProperty.Id) : Boolean {
        val ds = this.state.value
        if(ds.selectedFilePropertyIds == null) {
            return false
        }
        return if(ds.selectedFilePropertyIds.exists(id)) {
            deselect(id)
        }else{
            select(id)
        }
    }

    fun select(id: FileProperty.Id) : Boolean {
        return runCatching {
            this._state.value = this.state.value.let {
                it.copy(selectedFilePropertyIds = it.selectedFilePropertyIds?.addAndCopy(id))
            }
        }.isSuccess
    }

    fun deselect(id: FileProperty.Id) : Boolean {
        return runCatching {
            this._state.value = this.state.value.let {
                it.copy(selectedFilePropertyIds = it.selectedFilePropertyIds?.removeAndCopy(id))
            }
        }.isSuccess
    }


    fun pop() {
        val s = this.state.value
        this._state.value = s.copy(path = s.path.pop())
    }

    fun push(directory: Directory) {
        val s = this.state.value
        this._state.value = s.copy(path = s.path.push(directory))
    }

}