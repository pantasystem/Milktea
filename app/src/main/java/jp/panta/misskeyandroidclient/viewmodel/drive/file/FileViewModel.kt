package jp.panta.misskeyandroidclient.viewmodel.drive.file

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.api.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileViewModel(
    private val account: Account,
    private val misskeyAPI: MisskeyAPI,
    private val selectedFileMapLiveData: MutableLiveData<Map<FileProperty.Id, FileViewData>>?,
    private val maxSelectableItemSize: Int,
    folderId: String?,
    private val encryption: Encryption,
    private val filePropertyDataSource: FilePropertyDataSource
) : ViewModel(){

    val filesLiveData = MutableLiveData<List<FileViewData>>()
    val isSelectable = MutableLiveData<Boolean>(selectedFileMapLiveData != null)

    val isRefreshing = MutableLiveData<Boolean>(false)

    val currentFolder = MutableLiveData<String>(folderId)



    private var isLoading = false

    fun getSelectedItems(): List<FileViewData>?{
        //return selectedItemMap.values.toList()
        return selectedFileMapLiveData?.value?.values?.toList()
    }

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true
        isRefreshing.postValue(true)
        val request = RequestFile(i = account.getI(encryption), folderId = currentFolder.value, limit = 20)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching{
                val rawList = misskeyAPI.getFiles(request).throwIfHasError().body()
                if(rawList == null){
                    isLoading = false
                    isRefreshing.postValue(false)
                    return@launch
                }

                val entities  = rawList.map {
                    it.toFileProperty(account)
                }
                filePropertyDataSource.addAll(entities)
                entities.map{
                    FileViewData(it)
                }
            }.onSuccess { viewDataList ->
                filesLiveData.postValue(viewDataList)
                //selectedItemMap.clear()
                viewDataList.forEach{
                    //val selected = selectedItemMap[it.id]
                    val selected = selectedFileMapLiveData?.value?.get(it.id)
                    if(selected != null){
                        it.isSelect.postValue(true)
                    }else if((selectedFileMapLiveData?.value?.size ?: 0) >= maxSelectableItemSize){
                        it.isEnabledSelect.postValue(false)
                    }

                }

                isLoading = false
                isRefreshing.postValue(false)
            }.onFailure {
                isLoading = false
                isRefreshing.postValue(false)
            }
        }

    }

    fun loadNext(){
        if(isLoading){
            return
        }
        isLoading = true
        val beforeList = filesLiveData.value
        val untilId  = beforeList?.lastOrNull()?.id
        if(beforeList == null || untilId == null){
            isLoading = false
            return
        }
        val request = RequestFile(i = account.getI(encryption), folderId = currentFolder.value, limit = 20, untilId = untilId.fileId)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val rawList = misskeyAPI.getFiles(request).throwIfHasError().body()

                requireNotNull(rawList)
                require(rawList.isNotEmpty())
                val entities = rawList.map {
                    it.toFileProperty(account)
                }
                filePropertyDataSource.addAll(entities)

                entities.map{
                    FileViewData(it).apply{
                        val selected = selectedFileMapLiveData?.value?.get(it.id)
                        if(selected != null){
                            isSelect.postValue(true)
                        }else{
                            isEnabledSelect.postValue(
                                (selectedFileMapLiveData?.value?.size ?: 0) < maxSelectableItemSize
                            )
                        }
                    }
                }
            }.onSuccess { viewDataList ->
                filesLiveData.postValue(ArrayList(beforeList).also {
                    it.addAll(viewDataList)
                })
            }.onFailure {
                isLoading = false
            }

        }

    }

    fun changeSelectItemState(fileViewData: FileViewData){
        /*if(selectedItemMap.size >= maxSelectableItemSize)
            return*/

        //nullはfalseとして扱う
        val tmp = selectedFileMapLiveData?.value
        val selectedItemMap =
            if(tmp != null){
            HashMap<FileProperty.Id, FileViewData>(tmp)
        }else{
            HashMap()
        }
        val isSelect = fileViewData.isSelect.value
        if(isSelect == null){
            fileViewData.isSelect.postValue(true)
            selectedItemMap[fileViewData.id] = fileViewData
            selectedFileMapLiveData?.value = selectedItemMap
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
            return
        }

        if(isSelect){
            selectedItemMap.remove(fileViewData.id)
            fileViewData.isSelect.postValue(false)
            selectedFileMapLiveData?.value = selectedItemMap
            allEnabledSelect()
            Log.d("FileViewModel", "解除した")
            /*if(selectedItemMap.size < maxSelectableItemSize){
            }*/
        }else{
            selectedItemMap[fileViewData.id] = fileViewData
            fileViewData.isSelect.postValue(true)
            selectedFileMapLiveData?.value = selectedItemMap
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
        }
    }

    private fun allDisabledSelect(){
        filesLiveData.value?.forEach{
            //val item = selectedItemMap[it.id]
            val item = selectedFileMapLiveData?.value?.get(it.id)
            if(item == null){
                it.isEnabledSelect.postValue(false)
            }
        }
    }

    private fun allEnabledSelect(){
        filesLiveData.value?.forEach{
            it.isEnabledSelect.postValue(true)
        }
    }

    fun uploadFile(file: jp.panta.misskeyandroidclient.model.file.File, fileUploader: FileUploader){
        val uploadFile = file.copy(folderId = currentFolder.value)

        viewModelScope.launch(Dispatchers.IO) {
            try{
                fileUploader.upload(uploadFile, true).let {
                    filePropertyDataSource.add(it.toFileProperty(account))
                }
            }catch(e: Exception){
                Log.d("DriveViewModel", "ファイルアップロードに失敗した")
            }
        }
    }



}