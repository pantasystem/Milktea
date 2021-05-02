package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.drive.CreateFolder
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderViewModel(
    val account: Account,
    val misskeyAPI: MisskeyAPI,
    folderId: String?,
    private val encryption: Encryption
) : ViewModel(){


    val foldersLiveData = MutableLiveData<List<FolderViewData>>()

    val isRefreshing = MutableLiveData<Boolean>(false)

    val currentFolder = MutableLiveData<String>(folderId)

    private var isLoading = false

    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true

        isRefreshing.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val rawList = misskeyAPI.getFolders(RequestFolder(i = account.getI(encryption), folderId = currentFolder.value, limit = 20)).throwIfHasError().body()
                requireNotNull(rawList)
                require(rawList.isNotEmpty())
                rawList.map{
                    FolderViewData(it)
                }
            }.onSuccess {
                foldersLiveData.postValue(it)
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

        val request = RequestFolder(i = account.getI(encryption), folderId = currentFolder.value, limit = 20, untilId = untilId)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                misskeyAPI.getFolders(request).throwIfHasError().body()?.map {
                    FolderViewData(it)
                }

            }.onSuccess { viewDataList ->
                requireNotNull(viewDataList)
                val newList = ArrayList<FolderViewData>(beforeList).apply{
                    addAll(viewDataList)
                }
                foldersLiveData.postValue(newList)
            }.onFailure {

            }
            isLoading = false
        }

    }

    fun createFolder(folderName: String){
        if(folderName.isNotBlank()){
            viewModelScope.launch {
                runCatching {
                    misskeyAPI.createFolder(CreateFolder(
                        i = account.getI(encryption),
                        name = folderName,
                        parentId = currentFolder.value
                    )).throwIfHasError().body()

                }.onFailure {
                    Log.e("FolderViewModel", "error create folder", it)
                    _error.value = it
                }
            }

        }

    }
}