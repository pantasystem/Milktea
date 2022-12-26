package net.pantasystem.milktea.app_store.drive

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DirectoryPath
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.SelectedFilePropertyIds


data class DriveState (
    val path: DirectoryPath,
    val accountId: Long?,
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
        return runCancellableCatching {
            this._state.value = this.state.value.let {
                it.copy(selectedFilePropertyIds = it.selectedFilePropertyIds?.addAndCopy(id))
            }
        }.isSuccess
    }

    fun deselect(id: FileProperty.Id) : Boolean {
        return runCancellableCatching {
            this._state.value = this.state.value.let {
                it.copy(selectedFilePropertyIds = it.selectedFilePropertyIds?.removeAndCopy(id))
            }
        }.isSuccess
    }


    fun pop() : Boolean{
        val s = this.state.value
        val p = s.path.pop()
        this._state.value = s.copy(path = p)
        return p != s.path
    }

    fun popUntil(directory: Directory?) {
        val s = this.state.value
        this._state.value = s.copy(path = s.path.popUntil(directory))
    }

    fun push(directory: Directory) {
        val s = this.state.value
        this._state.value = s.copy(path = s.path.push(directory))
    }


    fun setAccount(account: Account) {
        if(this.state.value.accountId == account.accountId) {
            return
        }
        this._state.value = this.state.value.copy(
            accountId = account.accountId,
            selectedFilePropertyIds = this.state.value.selectedFilePropertyIds?.clearSelectedIdsAndCopy(),
            path = this.state.value.path.clear()
        )

    }
}