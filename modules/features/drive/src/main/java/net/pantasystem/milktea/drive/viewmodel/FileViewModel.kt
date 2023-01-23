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
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.drive.DriveStore
import net.pantasystem.milktea.app_store.drive.FilePropertyPagingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
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

    private val _fileCardDropDowned = MutableStateFlow<FileProperty.Id?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filesState = filePropertyPagingStore.state.flatMapLatest { pageableState ->
        val ids = when (val content = pageableState.content) {
            is StateContent.Exist -> content.rawContent
            is StateContent.NotExist -> emptyList()
        }
        filePropertyDataSource.observeIn(ids).map { list ->
            pageableState.convert {
                list
            }
        }
    }.distinctUntilChanged().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )

    val state = combine(
        filesState,
        driveStore.state,
        _fileCardDropDowned
    ) { p, driveState, dropDownedFileId ->
        p.convert {
            it.map { property ->
                FileViewData(
                    property,
                    driveState.selectedFilePropertyIds?.exists(property.id) == true,
                    driveState.isSelectMode
                            && (driveState.selectedFilePropertyIds?.exists(property.id) == true
                                || driveState.selectedFilePropertyIds?.isAddable == true),
                    isDropdownMenuExpanded = dropDownedFileId == property.id
                )
            }
        }
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )

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
        viewModelScope.launch {
            runCancellableCatching {
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
        viewModelScope.launch {
            runCancellableCatching {
                filePropertyPagingStore.loadPrevious()
            }.onFailure {
                _error.value = it
            }
        }
    }


    fun uploadFile(file: AppFile.Local) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            try {
                filePropertyRepository.toggleNsfw(id)
            } catch (e: Exception) {
                logger.info("nsfwの更新に失敗しました", e = e)
            }
        }
    }


    fun deleteFile(id: FileProperty.Id) {
        viewModelScope.launch {
            filePropertyRepository.delete(id).onFailure { e ->
                logger.info("ファイルの削除に失敗しました", e = e)
            }
        }
    }

    fun updateCaption(id: FileProperty.Id, newCaption: String) {
        viewModelScope.launch {

            filePropertyRepository.update(
                filePropertyRepository.find(id)
                    .update(comment = newCaption)
            ).onFailure {
                logger.info("キャプションの更新に失敗しました。", e = it)
            }
        }
    }

    fun updateFileName(id: FileProperty.Id, name: String) {
        viewModelScope.launch {
            filePropertyRepository.update(
                filePropertyRepository.find(id)
                    .update(name = name)
            ).onFailure {
                logger.error("update file name failed", it)
            }
        }
    }
    fun openFileCardDropDownMenu(fileId: FileProperty.Id) {
        _fileCardDropDowned.value = fileId
    }

    fun closeFileCardDropDownMenu() {
        _fileCardDropDowned.value = null
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