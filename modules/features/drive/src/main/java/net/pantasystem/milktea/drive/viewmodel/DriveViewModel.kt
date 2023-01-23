package net.pantasystem.milktea.drive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.drive.DriveStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import java.io.Serializable


data class DriveSelectableMode(
    val selectableMaxSize: Int,
    val selectedFilePropertyIds: List<FileProperty.Id>,
    val accountId: Long
) : Serializable

class DriveViewModel @AssistedInject constructor(
    private val configRepository: LocalConfigRepository,
    loggerFactory: Logger.Factory,
    @Assisted val driveStore: DriveStore,
    @Assisted val selectable: DriveSelectableMode?,
) : ViewModel() {

    companion object;
    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(driveStore: DriveStore, selectable: DriveSelectableMode?): DriveViewModel
    }

    private val logger = loggerFactory.create("DriveViewModel")

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

    val isUsingGridView = configRepository.observe().map {
        it.isDriveUsingGridView
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)


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

    fun setUsingGridView(value: Boolean) {
        viewModelScope.launch {
            configRepository.get().mapCancellableCatching { config ->
                configRepository.save(
                    config.copy(isDriveUsingGridView = value)
                )
            }.onFailure {
                logger.error("setUsingGridView error value:$value", it)
            }
        }
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