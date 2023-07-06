package net.pantasystem.milktea.drive.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.drive.DriveDirectoryPagingStore
import net.pantasystem.milktea.app_store.drive.FilePropertyPagingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.convert
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common_navigation.EXTRA_ACCOUNT_ID
import net.pantasystem.milktea.common_navigation.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.drive.CreateDirectory
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DirectoryId
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@HiltViewModel
class DriveViewModel @Inject constructor(
    private val directoryRepository: DriveDirectoryRepository,
    private val accountStore: AccountStore,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val accountRepository: AccountRepository,
    private val configRepository: LocalConfigRepository,
    private val savedStateHandle: SavedStateHandle,
    private val directoryPagingStore: DriveDirectoryPagingStore,
    private val filePagingStore: FilePropertyPagingStore,
    private val filePropertyRepository: DriveFileRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    companion object {
        const val STATE_CURRENT_DIRECTORY_ID = "STATE_CURRENT_DIRECTORY_ID"
        const val STATE_SELECTABLE_MODE = "STATE_SELECTABLE_MODE"
        const val STATE_SELECTED_FILE_PROPERTY_IDS = "STATE_SELECTED_FILE_PROPERTY_IDS"
    }

    private val logger = loggerFactory.create("DriveViewModel")

    private val currentDirectoryStrId = savedStateHandle.getStateFlow<String?>(
        STATE_CURRENT_DIRECTORY_ID,
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(it)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val maxSelectableSize = savedStateHandle.getStateFlow<Int?>(
        EXTRA_INT_SELECTABLE_FILE_MAX_SIZE,
        null,
    )

    private val currentDirectory = combine(
        currentDirectoryStrId,
        currentAccount,
    ) { directoryStrId, account ->
        directoryStrId?.let { dirId ->
            account?.let { ac ->
                directoryRepository.findOne(DirectoryId(ac.accountId, dirId)).getOrThrow()
            }
        }
    }.catch {
        logger.error("currentDirectory error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val directoriesState = directoryPagingStore.state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )

    private val selectedFileIds = savedStateHandle.getStateFlow<List<FileProperty.Id>>(
        STATE_SELECTED_FILE_PROPERTY_IDS,
        emptyList(),
    )

    private val fState = filePagingStore.state.convert {
        filePropertyDataSource.observeIn(it ?: emptyList())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )
    private val _fileCardDropDowned = MutableStateFlow<FileProperty.Id?>(null)

    private val filesState = combine(
        fState,
        selectedFileIds,
        _fileCardDropDowned,
        maxSelectableSize
    ) { files, selected, dropdown, maxSize ->
        files.convert { state ->
            state.map {
                FileViewData(
                    fileProperty = it,
                    isSelected = selected.contains(it.id),
                    isDropdownMenuExpanded = dropdown == it.id,
                    isEnabled = (maxSize == null || selected.size < maxSize) || selected.contains(it.id),
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )

    private val isSelectMode = savedStateHandle.getStateFlow<Boolean>(
        STATE_SELECTABLE_MODE,
        false,
    )

    val isUsingGridView = configRepository.observe().map {
        it.isDriveUsingGridView
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)


    private val modes = combine(isSelectMode, maxSelectableSize, isUsingGridView) { select, size, grid ->
        Modes(
            isSelectMode = select,
            maxSelectableSize = size,
            actionMode = if (select) DriveUiState.ActionMode.Selectable else DriveUiState.ActionMode.Normal,
            viewMode = if (grid) DriveUiState.ViewMode.Grid else DriveUiState.ViewMode.List,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Modes()
    )

    private val pagingState = combine(
        directoriesState,
        filesState,
    ) { dState, fState ->
        PagingState(
            directoriesState = dState,
            driveFilesState = fState,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PagingState()
    )

    val uiState = combine(
        currentAccount,
        currentDirectory,
        pagingState,
        modes,
        selectedFileIds,
    ) { ac, dir, pagingState, mode, selected ->
        DriveUiState(
            currentAccount = ac,
            currentDirectory = dir,
            directoriesState = pagingState.directoriesState,
            driveFilesState = pagingState.driveFilesState,
            selectedFilePropertyIds = selected,
            actionMode = mode.actionMode,
            viewMode = mode.viewMode,
            isSelectMode = mode.isSelectMode || mode.maxSelectableSize != null && mode.maxSelectableSize > 0,
            maxSelectableSize = mode.maxSelectableSize,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DriveUiState()
    )

    init {
        combine(currentAccount, currentDirectory) { ac, dir ->
            ac to dir
        }.onEach { (ac, dir) ->
            refreshPagingState(ac, dir)
        }.launchIn(viewModelScope)
    }

    fun getSelectedFileIds(): List<FileProperty.Id>? {
        return savedStateHandle.get<List<FileProperty.Id>>(STATE_SELECTED_FILE_PROPERTY_IDS)
    }


    fun push(directory: Directory) {
        savedStateHandle[STATE_CURRENT_DIRECTORY_ID] = directory.id.directoryId
        logger.debug {
            "push directory:${directory.name} id:${directory.id.directoryId} parentId:${directory.parent?.id?.directoryId}"
        }
    }


    fun pop(): Boolean {
        val currentDir = currentDirectory.value
        val dir = currentDir?.parentId?.directoryId
        savedStateHandle[STATE_CURRENT_DIRECTORY_ID] = dir

        return currentDir != null
    }

    fun popUntil(directory: Directory?) {
        savedStateHandle[STATE_CURRENT_DIRECTORY_ID] = directory?.parent?.parent?.id
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

    fun onFileListViewBottomReached() {
        viewModelScope.launch {
            filePagingStore.loadPrevious().onFailure {
                logger.error("onFileListViewBottomReached error", it)
            }
        }
    }

    fun onDirectoryListViewBottomReached() {
        viewModelScope.launch {
            directoryPagingStore.loadPrevious().onFailure {
                logger.error("onDirectoryListViewBottomReached error", it)
            }
        }
    }

    fun onDirectoryListRefreshed() {
        viewModelScope.launch {
            directoryPagingStore.clear()
            directoryPagingStore.loadPrevious().onFailure {
                logger.error("onDirectoryListRefreshed error", it)
            }
        }
    }

    fun onFileListRefreshed() {
        viewModelScope.launch {
            filePagingStore.clear()
            filePagingStore.loadPrevious().onFailure {
                logger.error("onFileListRefreshed error", it)
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

    fun toggleSelect(id: FileProperty.Id) {
        val maxSelectableSize: Int? = savedStateHandle[EXTRA_INT_SELECTABLE_FILE_MAX_SIZE]
        val newList = (savedStateHandle.get<List<FileProperty.Id>>(STATE_SELECTED_FILE_PROPERTY_IDS) ?: emptyList()).let { list ->
            if (list.contains(id)) {
                list - id
            } else {
                if (list.size >= (maxSelectableSize ?: Int.MAX_VALUE)) {
                    list
                } else {
                    list + id
                }
            }
        }
        savedStateHandle[STATE_SELECTED_FILE_PROPERTY_IDS] = newList
        if (!uiState.value.requireSelectedResult && newList.isEmpty()) {
            savedStateHandle[STATE_SELECTABLE_MODE] = false
        }
    }

    fun uploadFile(file: AppFile.Local) {
        viewModelScope.launch {
            try {
                val currentDir = getCurrentDirId()
                val accountId = savedStateHandle.get<Long>(EXTRA_ACCOUNT_ID)
                    ?: accountRepository.getCurrentAccount().getOrThrow().accountId
                val e = filePropertyRepository.create(
                    accountId,
                    file.copy(folderId = currentDir)
                ).getOrThrow()
                filePagingStore.onCreated(e.id)
            } catch (e: Exception) {
                logger.info("ファイルアップロードに失敗した")
            }
        }
    }


    fun createDirectory(folderName: String) {
        if (folderName.isNotBlank()) {
            viewModelScope.launch {
                val accountId = savedStateHandle.get<Long>(EXTRA_ACCOUNT_ID)
                    ?: accountRepository.getCurrentAccount().getOrNull()?.accountId
                    ?: return@launch
                val currentDir = getCurrentDirId()

                directoryRepository.create(
                    CreateDirectory(
                        accountId = accountId,
                        directoryName = folderName,
                        parentId = currentDir,
                    )
                ).onFailure {
                    logger.error("error create folder", it)
                }.onSuccess {
                    directoryPagingStore.onCreated(it)
                }
            }

        }

    }

    fun onFileMoveToHereButtonClicked() {
        viewModelScope.launch {
            val currentDir = getCurrentDirId()
            val selectedFileIds = getSelectedFileIds()
            if (selectedFileIds.isNullOrEmpty()) {
                return@launch
            }
            selectedFileIds.map { id ->
                filePropertyRepository.update(
                    filePropertyRepository.find(id).update(
                        folderId = currentDir
                    )
                ).onFailure {
                    logger.error("error move file", it)
                }
            }
            savedStateHandle[STATE_SELECTED_FILE_PROPERTY_IDS] = emptyList<FileProperty.Id>()
            if (!uiState.value.requireSelectedResult) {
                savedStateHandle[STATE_SELECTABLE_MODE] = false
            }
            refresh()
        }
    }

    fun selectAndSelectMode(fileProperty: FileProperty) {
        if (uiState.value.requireSelectedResult) {
            return
        }
        if (uiState.value.isSelectMode) {
            return
        }
        savedStateHandle[STATE_SELECTABLE_MODE] = true
        toggleSelect(fileProperty.id)
    }

    private fun getCurrentDirId(): String? {
        return savedStateHandle.get<String>(STATE_CURRENT_DIRECTORY_ID)
    }

    private suspend fun refreshPagingState(account: Account?, currentDirectory: Directory?) {
        filePagingStore.setCurrentDirectory(currentDirectory)
        directoryPagingStore.setCurrentDirectory(currentDirectory)

        filePagingStore.setCurrentAccount(account)
        directoryPagingStore.setAccount(account)

        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            filePagingStore.clear()
            filePagingStore.loadPrevious().onFailure {
                logger.error("refreshPagingState error", it)
            }
        }

        viewModelScope.launch {
            directoryPagingStore.clear()
            directoryPagingStore.loadPrevious().onFailure {
                logger.error("refreshPagingState error", it)
            }
        }
    }
}

data class DriveUiState(
    val currentAccount: Account? = null,
    val currentDirectory: Directory? = null,
    val directoriesState: PageableState<List<Directory>> = PageableState.Loading.Init(),
    val driveFilesState: PageableState<List<FileViewData>> = PageableState.Loading.Init(),
    val actionMode: ActionMode = ActionMode.Normal,
    val viewMode: ViewMode = ViewMode.List,
    val selectedFilePropertyIds: List<FileProperty.Id> = emptyList(),
    val maxSelectableSize: Int? = null,
    val isSelectMode: Boolean = false,
) {
    enum class ActionMode {
        Normal,
        Selectable,
    }

    enum class ViewMode {
        List,
        Grid,
    }

    val requireSelectedResult: Boolean by lazy {
        isSelectMode && maxSelectableSize != null
    }

    val canFileMove: Boolean by lazy {
        isSelectMode && maxSelectableSize == null && selectedFilePropertyIds.isNotEmpty()
    }
}

private data class Modes(
    val isSelectMode: Boolean = false,
    val maxSelectableSize: Int? = null,
    val actionMode: DriveUiState.ActionMode = DriveUiState.ActionMode.Normal,
    val viewMode: DriveUiState.ViewMode = DriveUiState.ViewMode.List,
)

private data class PagingState(
    val directoriesState: PageableState<List<Directory>> = PageableState.Loading.Init(),
    val driveFilesState: PageableState<List<FileViewData>> = PageableState.Loading.Init(),
)