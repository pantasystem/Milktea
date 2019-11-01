package jp.panta.misskeyandroidclient.viewmodel.drive.file

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class FileViewModel(
    private val connectionInstance: ConnectionInstance,
    private val misskeyAPI: MisskeyAPI,
    isSelectable: Boolean,
    private val maxSelectableItemSize: Int,
    private val folderId: String?
) : ViewModel(){

    val filesLiveData = MutableLiveData<List<FileViewData>>()
    val isSelectable = MutableLiveData<Boolean>(isSelectable)

    val isRefreshing = MutableLiveData<Boolean>(false)


    private val selectedItemMap = HashMap<String, FileViewData>()

    private var isLoading = false

    fun getSelectedItems(): List<FileViewData>{
        return selectedItemMap.values.toList()
    }

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true
        isRefreshing.postValue(true)
        val request = RequestFile(i = connectionInstance.getI()!!, folderId = folderId, limit = 20)
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
        val request = RequestFile(i = connectionInstance.getI()!!, folderId = folderId, limit = 20, untilId = untilId)
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
                        FileViewData(it)
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
        if(selectedItemMap.size >= maxSelectableItemSize)
            return

        //nullはfalseとして扱う
        val isSelect = fileViewData.isSelect.value
        if(isSelect == null){
            fileViewData.isSelect.value = true
            selectedItemMap[fileViewData.id] = fileViewData
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
            return
        }

        if(isSelect){
            selectedItemMap.remove(fileViewData.id)
            fileViewData.isSelect.value = false
            if(selectedItemMap.size < maxSelectableItemSize){
                allEnabledSelect()
            }
        }else{
            selectedItemMap[fileViewData.id] = fileViewData
            fileViewData.isSelect.value = true
            if(selectedItemMap.size >= maxSelectableItemSize){
                allDisabledSelect()
            }
        }
    }

    private fun allDisabledSelect(){
        filesLiveData.value?.forEach{
            val item = selectedItemMap[it.id]
            if(item == null){
                it.isEnabledSelect.value = false
            }
        }
    }

    private fun allEnabledSelect(){
        filesLiveData.value?.forEach{
            it.isEnabledSelect.value = true
        }
    }


}