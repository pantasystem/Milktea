
package jp.panta.misskeyandroidclient.ui.drive.viewmodel.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.file.AppFile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.drive.FilePropertyPagingStore
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.DriveStore
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource

/**
 * 選択状態とFileの読み込み＆表示を担当する
 */
class FileViewModel @AssistedInject constructor(
    private val accountRepository: AccountRepository,
    loggerFactory: Logger.Factory,
    filePropertyDataSource: FilePropertyDataSource,
    private val filePropertyRepository: DriveFileRepository,
    @Assisted filePropertyPagingStoreFactory: FilePropertyPagingStore.AssistedStoreFactory,
    @Assisted private val driveStore: DriveStore,
) : ViewModel() {

    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(filePropertyPagingStoreFactory: FilePropertyPagingStore.AssistedStoreFactory, driveStore: DriveStore): FileViewModel
    }

    companion object;

    private val currentAccountWatcher: CurrentAccountWatcher by lazy {
        CurrentAccountWatcher(driveStore.state.value.accountId, accountRepository)
    }

    val logger by lazy {
        loggerFactory.create("FileViewModel")
    }

    private val filePropertiesPagingStore by lazy {
        filePropertyPagingStoreFactory.create(
            driveStore.state.value.path.path.lastOrNull()?.id,
        ) { currentAccountWatcher.getAccount() }
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
        filePropertiesPagingStore.state.map { pageable ->
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
            filePropertiesPagingStore.setCurrentDirectory(it.path.lastOrNull())
            filePropertiesPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)

        /**
         * アカウントの状態をDirectoryPath, FilePropertiesPagingStoreへ伝達します。
         */
        account.distinctUntilChangedBy {
            it.accountId
        }.onEach {

            driveStore.setAccount(it)
            filePropertiesPagingStore.clear()
            filePropertiesPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)


    }

    fun loadInit() {
        if (filePropertiesPagingStore.isLoading) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                filePropertiesPagingStore.clear()
                filePropertiesPagingStore.loadPrevious()
            }.onFailure {
                _error.value = it
            }
        }

    }

    fun loadNext() {
        if (filePropertiesPagingStore.isLoading) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                filePropertiesPagingStore.loadPrevious()
            }.onFailure {
                _error.value = it
            }
        }
    }


    fun uploadFile(file: AppFile.Local) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val account = currentAccountWatcher.getAccount()
                filePropertyRepository.create(account.accountId, file)
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


}


@Suppress("UNCHECKED_CAST")
fun FileViewModel.Companion.provideFactory(
    factory: FileViewModel.AssistedViewModelFactory,
    filePropertyPagingStoreFactory: FilePropertyPagingStore.AssistedStoreFactory,
    driveStore: DriveStore
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(filePropertyPagingStoreFactory, driveStore) as T
    }

}