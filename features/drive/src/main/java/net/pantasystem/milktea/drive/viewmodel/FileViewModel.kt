package net.pantasystem.milktea.drive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.drive.*
import net.pantasystem.milktea.model.file.AppFile

/**
 * 選択状態とFileの読み込み＆表示を担当する
 */
class FileViewModel @AssistedInject constructor(
    private val accountRepository: AccountRepository,
    loggerFactory: Logger.Factory,
    filePropertyDataSource: FilePropertyDataSource,
    private val filePropertyPagingStore: FilePropertyPagingStore,
    private val filePropertyRepository: DriveFileRepository,
    private val accountStore: AccountStore,
    @Assisted private val driveStore: DriveStore,
) : ViewModel() {

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(
            driveStore: DriveStore
        ): FileViewModel
    }

    companion object;

    private val currentAccountWatcher: CurrentAccountWatcher by lazy {
        CurrentAccountWatcher(driveStore.state.value.accountId, accountRepository)
    }

    val logger by lazy {
        loggerFactory.create("FileViewModel")
    }

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> get() = _error


    val selectedFileIds = this.driveStore.state.map {
        it.selectedFilePropertyIds?.selectedIds
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val account =
        currentAccountWatcher.account.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = filePropertyDataSource.state.flatMapLatest { state ->
        filePropertyPagingStore.state.map { pageable ->
            pageable.convert {
                state.findIn(it)
            }
        }
    }.combine(driveStore.state) { p, driveState ->
        p.convert {
            it.map { property ->
                FileViewData(
                    property,
                    driveState.selectedFilePropertyIds?.exists(property.id) == true,
                    driveState.isSelectMode && (driveState.selectedFilePropertyIds?.exists(property.id) == true || driveState.selectedFilePropertyIds?.isAddable == true)
                )
            }
        }
    }


    init {

        driveStore.state.map {
            it.path
        }.distinctUntilChangedBy {
            it.path.lastOrNull()?.id
        }.onEach {
            filePropertyPagingStore.setCurrentAccount(
                accountStore.currentAccount
            )
            filePropertyPagingStore.setCurrentDirectory(it.path.lastOrNull())
            filePropertyPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)



        /**
         * アカウントの状態をDirectoryPath, FilePropertiesPagingStoreへ伝達します。
         */
        account.distinctUntilChangedBy {
            it.accountId
        }.onEach {
            driveStore.setAccount(it)
            filePropertyPagingStore.setCurrentAccount(it)
            filePropertyPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    fun loadInit() {
        if (filePropertyPagingStore.isLoading) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                filePropertyPagingStore.clear()
                filePropertyPagingStore.loadPrevious()
            }.onFailure {
                _error.value = it
            }
        }

    }

    fun loadNext() {
        if (filePropertyPagingStore.isLoading) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                filePropertyPagingStore.loadPrevious()
            }.onFailure {
                _error.value = it
            }
        }
    }


    fun uploadFile(file: AppFile.Local) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentDir = driveStore.state.value.path.path.lastOrNull()?.id
                val account = currentAccountWatcher.getAccount()
                val e = filePropertyRepository.create(
                    account.accountId,
                    file.copy(folderId = currentDir)
                ).getOrThrow()
                filePropertyPagingStore.onCreated(e.id)
            } catch (e: Exception) {
                logger.info("ファイルアップロードに失敗した")
            }
        }
    }

    fun toggleNsfw(id: FileProperty.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                filePropertyRepository.toggleNsfw(id)
            } catch (e: Exception) {
                logger.info("nsfwの更新に失敗しました", e = e)
            }
        }
    }


    fun deleteFile(id: FileProperty.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            filePropertyRepository.delete(id).onFailure { e ->
                logger.info("ファイルの削除に失敗しました", e = e)
            }
        }
    }

    fun updateCaption(id: FileProperty.Id, newCaption: String) {
        viewModelScope.launch(Dispatchers.IO) {

            filePropertyRepository.update(
                filePropertyRepository.find(id)
                    .update(comment = newCaption)
            ).onFailure {
                logger.info("キャプションの更新に失敗しました。", e = it)
            }
        }
    }


}


@Suppress("UNCHECKED_CAST")
fun FileViewModel.Companion.provideFactory(
    factory: FileViewModel.AssistedViewModelFactory,
    driveStore: DriveStore
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(driveStore) as T
    }

}