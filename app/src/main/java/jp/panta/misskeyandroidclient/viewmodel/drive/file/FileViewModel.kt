package jp.panta.misskeyandroidclient.viewmodel.drive.file

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.UploadFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileViewModel(
    private val accountRelation: AccountRelation,
    private val misskeyAPI: MisskeyAPI,
    private val selectedFileMapLiveData: MutableLiveData<Map<String, FileViewData>>?,
    private val maxSelectableItemSize: Int,
    folderId: String?,
    private val encryption: Encryption
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
        val request = RequestFile(i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!, folderId = currentFolder.value, limit = 20)
        misskeyAPI.getFiles(request).enqueue(object : Callback<List<FileProperty>>{
            override fun onResponse(
                call: Call<List<FileProperty>>,
                response: Response<List<FileProperty>>
            ) {
                val rawList = response.body()
                if(rawList == null){
                    isLoading = false
                    isRefreshing.postValue(false)
                    return
                }

                val viewDataList = rawList.map{
                    FileViewData(it)
                }

                filesLiveData.postValue(viewDataList)
                //selectedItemMap.clear()
                viewDataList.forEach{
                    //val selected = selectedItemMap[it.id]
                    val selected = selectedFileMapLiveData?.value?.get(it.id)
                    if(selected != null){
                        it.isSelect.postValue(true)
                    }else if(selectedFileMapLiveData?.value?.size?: 0 >= maxSelectableItemSize){
                        it.isEnabledSelect.postValue(false)
                    }

                }

                isLoading = false
                isRefreshing.postValue(false)

            }

            override fun onFailure(call: Call<List<FileProperty>>, t: Throwable) {
                isLoading = false
                isRefreshing.postValue(false)
            }
        })
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
        val request = RequestFile(i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!, folderId = currentFolder.value, limit = 20, untilId = untilId)
        misskeyAPI.getFiles(request).enqueue(object : Callback<List<FileProperty>>{
            override fun onResponse(
                call: Call<List<FileProperty>>,
                response: Response<List<FileProperty>>
            ) {
                val rawList = response.body()
                if(rawList == null || rawList.isEmpty()){
                    isLoading = false
                    return
                }

                val viewDataList = ArrayList<FileViewData>(beforeList).apply{
                    addAll(rawList.map{
                        FileViewData(it).apply{
                            val selected = selectedFileMapLiveData?.value?.get(it.id)
                            if(selected != null){
                                isSelect.postValue(true)
                            }else{
                                isEnabledSelect.postValue(selectedFileMapLiveData?.value?.size?:0  < maxSelectableItemSize)
                            }
                        }
                    })
                }

                filesLiveData.postValue(viewDataList)
                isLoading = false
            }

            override fun onFailure(call: Call<List<FileProperty>>, t: Throwable) {
                isLoading = false
            }
        })
    }

    fun changeSelectItemState(fileViewData: FileViewData){
        /*if(selectedItemMap.size >= maxSelectableItemSize)
            return*/

        //nullはfalseとして扱う
        val tmp = selectedFileMapLiveData?.value
        val selectedItemMap =
            if(tmp != null){
            HashMap<String, FileViewData>(tmp)
        }else{
            HashMap<String, FileViewData>()
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
                fileUploader.upload(uploadFile, true)
            }catch(e: Exception){
                Log.d("DriveViewModel", "ファイルアップロードに失敗した")
            }
        }
    }



}