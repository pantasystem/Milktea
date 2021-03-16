package jp.panta.misskeyandroidclient.viewmodel.tags

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
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
        miCore.getCurrentAccount().filterNotNull().onEach {
            load()
        }.launchIn(viewModelScope + Dispatchers.IO)
    }
    fun load(){
        val account = miCore.getCurrentAccount().value
            ?:return
        isLoading.value = true
        val i = runCatching { account.getI(miCore.getEncryption()) }.getOrNull()
        if(i == null){
            isLoading.value = false
            return
        }

        miCore.getMisskeyAPI(account).getHashTagList(
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