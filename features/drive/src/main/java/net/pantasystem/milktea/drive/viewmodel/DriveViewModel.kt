package net.pantasystem.milktea.drive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.app_store.drive.DriveStore
import net.pantasystem.milktea.model.drive.*
import java.io.Serializable


data class DriveSelectableMode(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: List<FileProperty.Id>,
    val accountId: Long
) : Serializable

class DriveViewModel @AssistedInject constructor(
    @Assisted val driveStore: DriveStore,
    @Assisted val selectable: DriveSelectableMode?,
) : ViewModel() {

    companion object;

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(driveStore: DriveStore, selectable: DriveSelectableMode?): DriveViewModel
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


    val isSelectMode = driveStore.state.map {
        it.isSelectMode
    }


    fun getSelectedFileIds(): Set<FileProperty.Id>? {
        return this.driveStore.state.value.selectedFilePropertyIds?.selectedIds
    }


    fun push(directory: Directory) {
        this.driveStore.push(directory)
    }


    fun pop(): Boolean {
        val path = driveStore.state.value.path.path
        if (path.isEmpty()) {
            return false
        }

        return driveStore.pop()
    }

    fun popUntil(directory: Directory?) {
        driveStore.popUntil(directory)
    }


}

@Suppress("UNCHECKED_CAST")
fun DriveViewModel.Companion.provideViewModel(
    factory: DriveViewModel.AssistedViewModelFactory,
    driveStore: DriveStore,
    selectable: DriveSelectableMode?,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(driveStore, selectable) as T
    }

}