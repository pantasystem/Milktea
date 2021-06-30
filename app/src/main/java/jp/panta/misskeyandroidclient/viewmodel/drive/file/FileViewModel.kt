package jp.panta.misskeyandroidclient.viewmodel.drive.file

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.drive.RequestFile
import jp.panta.misskeyandroidclient.model.account.CurrentAccountWatcher
import jp.panta.misskeyandroidclient.model.drive.*
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
    currentDirectoryId: String?,
    private val miCore: MiCore,
    private val path: DirectoryPath,
    private val selectedFilePropertyIds: SelectedFilePropertyIds

) : ViewModel(){

    private val filePropertiesPagingStore = miCore.filePropertyPagingStore(null, currentDirectoryId)
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> get() = _error


    @ExperimentalCoroutinesApi
    private val account = currentAccountWatcher.account.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val state = filePropertiesPagingStore.state.map { state ->
        state.convert {
            runBlocking {
                it.map { id ->
                    miCore.getFilePropertyDataSource().find(id)
                }
            }
        }
    }.combine(selectedFilePropertyIds.state) { p, ids ->
        p.convert {
            it.map { property ->
                FileViewData(property, ids.contains(property.id))
            }
        }
    }


    init {
        /**
         * DirectoryPathの現在のCurrentDirをFilePropertiesPagingStoreへ伝達します。
         */
        path.route.onEach {
            filePropertiesPagingStore.setCurrentDirectory(it.lastOrNull())
            filePropertiesPagingStore.loadPrevious()
        }.launchIn(viewModelScope + Dispatchers.IO)

        /**
         * アカウントの状態をDirectoryPath, FilePropertiesPagingStoreへ伝達します。
         */
        account.distinctUntilChangedBy {
            it.accountId
        }.onEach {
            path.clear()
            filePropertiesPagingStore.setAccount(it)
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
        if (selectedFilePropertyIds.exists(id)) {
            selectedFilePropertyIds.remove(id)
        }else{
            selectedFilePropertyIds.add(id)
        }
    }

    fun uploadFile(file: File){
        val uploadFile = file.copy(folderId = path.route.value.lastOrNull()?.id)

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val account = currentAccountWatcher.getAccount()
                val uploader = miCore.getFileUploaderProvider().get(account)
                uploader.upload(uploadFile, true).let {
                    miCore.getFilePropertyDataSource().add(it.toFileProperty(account))
                }
            }catch(e: Exception){
                Log.d("DriveViewModel", "ファイルアップロードに失敗した")
            }
        }
    }



}