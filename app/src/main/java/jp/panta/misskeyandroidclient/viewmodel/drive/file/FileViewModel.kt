package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.drive.DeleteFileDTO
import jp.panta.misskeyandroidclient.api.drive.UpdateFileDTO
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.account.CurrentAccountWatcher
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 選択状態とFileの読み込み＆表示を担当する
 */
@ExperimentalCoroutinesApi
class FileViewModel(
    private val currentAccountWatcher: CurrentAccountWatcher,
    private val miCore: MiCore,
    private val driveStore: DriveStore,
) : ViewModel(){
    val logger = miCore.loggerFactory.create("FileViewModel")

    private val filePropertiesPagingStore = miCore.filePropertyPagingStore({ currentAccountWatcher.getAccount()}, driveStore.state.value.path.path.lastOrNull()?.id)
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> get() = _error



    val isAddable = this.driveStore.state.map {
        it.selectedFilePropertyIds?.isAddable == true
    }.asLiveData()

    val selectedFileIds = this.driveStore.state.map {
        it.selectedFilePropertyIds?.selectedIds
    }

    @ExperimentalCoroutinesApi
    private val account = currentAccountWatcher.account.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val state = miCore.getFilePropertyDataSource().state.flatMapLatest { state ->
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

    fun loadInit(){
        if(filePropertiesPagingStore.isLoading) {
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

    fun loadNext(){
        if(filePropertiesPagingStore.isLoading) {
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


    fun toggleSelect(id: FileProperty.Id) {
        driveStore.toggleSelect(id)
    }


    fun uploadFile(file: AppFile.Local){
        val uploadFile = file.copy(folderId = driveStore.state.value.path.path.lastOrNull()?.id)

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val account = currentAccountWatcher.getAccount()
                val uploader = miCore.getFileUploaderProvider().get(account)
                uploader.upload(uploadFile, true).let {
                    miCore.getFilePropertyDataSource().add(it.toFileProperty(account))
                }
            }catch(e: Exception){
                logger.info("ファイルアップロードに失敗した")
            }
        }
    }

    fun toggleNsfw(id: FileProperty.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                miCore.getDriveFileRepository().toggleNsfw(id)
            }catch(e: Exception) {
                logger.info("nsfwの更新に失敗しました", e = e)
            }
        }
    }



    fun deleteFile(id: FileProperty.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val account = currentAccountWatcher.getAccount()
                val api = miCore.getMisskeyAPIProvider().get(account)
                val fileProperty = miCore.getFilePropertyDataSource().find(id)
                api.deleteFile(DeleteFileDTO(i = account.getI(miCore.getEncryption()), fileId = id.fileId))
                    .throwIfHasError()
                miCore.getFilePropertyDataSource().remove(fileProperty)

            }catch(e: Exception) {
                logger.info("ファイルの削除に失敗しました", e = e)
            }
        }
    }




}