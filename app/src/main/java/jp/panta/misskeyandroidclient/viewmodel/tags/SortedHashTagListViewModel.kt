package jp.panta.misskeyandroidclient.viewmodel.tags

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class SortedHashTagListViewModel(
    val miCore: MiCore,
    val conditions: Conditions
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miCore: MiCore,
        val conditions: Conditions
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SortedHashTagListViewModel(
                miCore,
                conditions
            ) as T
        }
    }

    data class Conditions(
        val sort: String,
        val isAttachedToUserOnly: Boolean? = null,
        val isAttachedToLocalUserOnly: Boolean? = null,
        val isAttachedToRemoteUserOnly: Boolean? = null
    ): Serializable

    val hashTags = object : MediatorLiveData<List<HashTag>>(){

        override fun onActive() {
            super.onActive()

            if(value.isNullOrEmpty()){
                load()
            }
        }

    }

    val isLoading = MutableLiveData<Boolean>()

    init{
        hashTags.addSource(miCore.currentAccount){
            load()
        }
    }
    fun load(){
        isLoading.value = true
        val ci = miCore.currentAccount.value?.getCurrentConnectionInformation()
        val i = ci?.getI(miCore.getEncryption())
        if(i == null){
            isLoading.value = false
            return
        }

        miCore.getMisskeyAPI(ci).getHashTagList(
            RequestHashTagList(
                i = i,
                sort = conditions.sort,
                attachedToRemoteUserOnly = conditions.isAttachedToRemoteUserOnly,
                attachedToUserOnly = conditions.isAttachedToUserOnly,
                attachedToLocalUserOnly = conditions.isAttachedToLocalUserOnly
            )
        ).enqueue(object : Callback<List<HashTag>>{
            override fun onResponse(call: Call<List<HashTag>>, response: Response<List<HashTag>>) {
                hashTags.postValue(response.body())
                isLoading.postValue(false)
            }

            override fun onFailure(call: Call<List<HashTag>>, t: Throwable) {
                isLoading.postValue(false)
            }
        })
    }
}