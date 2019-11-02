package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FolderViewModel(
    val connectionInstance: ConnectionInstance,
    val misskeyAPI: MisskeyAPI,
    private val folderId: String?
) : ViewModel(){

    val foldersLiveData = MutableLiveData<List<FolderViewData>>()

    val isRefreshing = MutableLiveData<Boolean>(false)

    val currentFolder = MutableLiveData<String>(folderId)

    private var isLoading = false

    fun loadInit(){
        if(isLoading){
            return
        }
        isLoading = true

        isRefreshing.postValue(true)
        misskeyAPI.getFolders(RequestFolder(i = connectionInstance.getI()!!, folderId = currentFolder.value, limit = 20)).enqueue(object : Callback<List<FolderProperty>>{
            override fun onResponse(
                call: Call<List<FolderProperty>>,
                response: Response<List<FolderProperty>>
            ) {
                val rawList = response.body()
                if(rawList == null){
                    isLoading = false
                    isRefreshing.postValue(false)
                    return
                }

                val viewDataList = rawList.map{
                    FolderViewData(it)
                }

                foldersLiveData.postValue(viewDataList)
                isLoading = false
                isRefreshing.postValue(false)
            }

            override fun onFailure(call: Call<List<FolderProperty>>, t: Throwable) {
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
        val beforeList = foldersLiveData.value
        val untilId = beforeList?.lastOrNull()?.id
        if(beforeList == null || untilId == null){
            isLoading = false
            return
        }

        val request = RequestFolder(i = connectionInstance.getI()!!, folderId = currentFolder.value, limit = 20, untilId = untilId)
        misskeyAPI.getFolders(request).enqueue(object : Callback<List<FolderProperty>>{
            override fun onResponse(
                call: Call<List<FolderProperty>>,
                response: Response<List<FolderProperty>>
            ) {
                val rawList = response.body()
                if(rawList == null || rawList.isEmpty()){
                    isLoading = false
                    return
                }

                val viewDataList = rawList.map{
                    FolderViewData(it)
                }
                val newList = ArrayList<FolderViewData>(beforeList).apply{
                    addAll(viewDataList)
                }

                foldersLiveData.postValue(newList)
                isLoading = false

            }

            override fun onFailure(call: Call<List<FolderProperty>>, t: Throwable) {
                isLoading = false
            }
        })
    }
}