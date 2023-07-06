package net.pantasystem.milktea.drive.viewmodel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.drive.DriveDirectoryPagingStore
import net.pantasystem.milktea.app_store.drive.DriveStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.drive.CreateDirectory
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository


class DirectoryViewModel @AssistedInject constructor(
    loggerFactory: Logger.Factory,
    private val accountRepository: AccountRepository,
    private val driveDirectoryRepository: DriveDirectoryRepository,
    private val driveDirectoryPagingStore: DriveDirectoryPagingStore,
    accountStore: AccountStore,
    @Assisted private val driveStore: DriveStore,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(driveStore: DriveStore): DirectoryViewModel
    }

    companion object;

    private val accountWatcher by lazy {
        CurrentAccountWatcher(driveStore.state.value.accountId, accountRepository)
    }
    val foldersLiveData = driveDirectoryPagingStore.state.map { state ->
        state.convert { list ->
            list.map {
                DirectoryViewData(it)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, PageableState.Loading.Init())

    val isRefreshing = MutableLiveData(false)



    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    private val logger = loggerFactory.create("DirectoryVM")

    init {
        driveStore.state.map {
            it.accountId to it.path.path
        }.distinctUntilChanged().onEach {
            loadInit()
        }.catch { e ->
            logger.warning("アカウント変更伝達処理中にエラー", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

        driveStore.state.map {
            it.path.path.lastOrNull()
        }.onEach {
            driveDirectoryPagingStore.setCurrentDirectory(it)
            driveDirectoryPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)

        accountStore.state.map { it.currentAccount }.onEach {
            driveDirectoryPagingStore.setAccount(it)
            driveDirectoryPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun loadInit() {
        viewModelScope.launch {
            driveDirectoryPagingStore.clear()
            driveDirectoryPagingStore.loadPrevious()
        }
    }

    fun loadNext() {
        viewModelScope.launch {
            driveDirectoryPagingStore.loadPrevious()
        }
    }

    fun createDirectory(folderName: String) {
        if (folderName.isNotBlank()) {
            viewModelScope.launch {
                driveDirectoryRepository.create(
                    CreateDirectory(
                        accountId = accountWatcher.getAccount().accountId,
                        directoryName = folderName,
                        parentId = driveStore.state.value.path.path.lastOrNull()?.id?.directoryId
                    )
                ).onFailure {
                    Log.e("FolderViewModel", "error create folder", it)
                    _error.value = it
                }.onSuccess {
                    driveDirectoryPagingStore.onCreated(it)
                }
            }

        }

    }
}

@Suppress("UNCHECKED_CAST")
fun DirectoryViewModel.Companion.provideViewModel(
    assistedFactory: DirectoryViewModel.ViewModelAssistedFactory,
    driveStore: DriveStore,
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(driveStore) as T
    }

}