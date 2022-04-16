package jp.panta.misskeyandroidclient.ui.drive.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import net.pantasystem.milktea.common.Logger
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.drive.CreateFolder
import jp.panta.misskeyandroidclient.api.misskey.drive.RequestFolder
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.CurrentAccountWatcher
import jp.panta.misskeyandroidclient.model.drive.DriveStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class DirectoryViewModel(
    private val accountWatcher: CurrentAccountWatcher,
    private val driveStore: DriveStore,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption,
    val loggerFactory: net.pantasystem.milktea.common.Logger.Factory
) : ViewModel(){


    val foldersLiveData = MutableLiveData<List<DirectoryViewData>>()

    val isRefreshing = MutableLiveData(false)



    private var isLoading = false

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

    }

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true

        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = accountWatcher.getAccount()
                val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
                val rawList = misskeyAPI.getFolders(RequestFolder(i = account.getI(encryption), folderId = driveStore.state.value.path.path.lastOrNull()?.id, limit = 20)).throwIfHasError().body()
                requireNotNull(rawList)
                require(rawList.isNotEmpty())
                rawList.map{
                    DirectoryViewData(it)
                }
            }.onSuccess {
                foldersLiveData.postValue(it)
            }.onFailure {
                foldersLiveData.postValue(emptyList())
                logger.debug("初期ロードに失敗しました")
            }
            isLoading = false
            isRefreshing.postValue(false)
        }
    }

    fun loadNext(){
        if(isLoading){
            return
        }
        isLoading = true
        val beforeList = foldersLiveData.value
        val untilId = beforeList?.lastOrNull()?.id
        if(beforeList == null || untilId == null){
            isLoading = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = accountWatcher.getAccount()
                val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
                val request = RequestFolder(i = account.getI(encryption), folderId = driveStore.state.value.path.path.lastOrNull()?.id, limit = 20, untilId = untilId)

                misskeyAPI.getFolders(request).throwIfHasError().body()?.map {
                    DirectoryViewData(it)
                }

            }.onSuccess { viewDataList ->
                requireNotNull(viewDataList)
                val newList = ArrayList<DirectoryViewData>(beforeList).apply{
                    addAll(viewDataList)
                }
                foldersLiveData.postValue(newList)
            }.onFailure {
                logger.debug("loadNext中にエラー発生", e = it)
            }
            isLoading = false
        }

    }

    fun createFolder(folderName: String){
        if(folderName.isNotBlank()){
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val account = accountWatcher.getAccount()
                    val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
                    misskeyAPI.createFolder(CreateFolder(
                        i = account.getI(encryption),
                        name = folderName,
                        parentId = driveStore.state.value.path.path.lastOrNull()?.id
                    )).throwIfHasError().body()

                }.onFailure {
                    Log.e("FolderViewModel", "error create folder", it)
                    _error.value = it
                }
            }

        }

    }
}