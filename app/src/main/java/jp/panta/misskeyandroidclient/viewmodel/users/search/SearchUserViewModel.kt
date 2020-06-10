package jp.panta.misskeyandroidclient.viewmodel.users.search

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.SearchByUserAndHost
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UsersLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * SearchAndSelectUserViewModelを将来的にこのSearchUserViewModelと
 * SelectedUserViewModelに分離する予定
 */
class SearchUserViewModel(
    val miCore: MiCore,
    val hasDetail: Boolean?
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore, val hasDetail: Boolean?) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchUserViewModel(miCore, hasDetail) as T
        }
    }

    val userName = MutableLiveData<String>()
    val host = MutableLiveData<String>()

    private val users = UsersLiveData().apply{
        addSource(userName){
            search()
        }
        addSource(host){
            search()
        }
        addSource(miCore.currentAccount){
            setMainCapture(miCore.getMainCapture(it))
        }
    }

    private var mSearchByUserAndHost: SearchByUserAndHost? = null
    private var mNowInstanceBase: String? = null

    fun search(){

        val userName = this.userName.value?: return
        val host = this.host.value

        val request = RequestUser(
            i = getCi()?.getI(miCore.getEncryption())!!,
            userName = userName,
            userId = null,
            host = host,
            detail = hasDetail
        )

        getSearchByUserAndHost()?.search(request)?.enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                users.postValue(
                    response.body()?.map{
                        UserViewData(it)
                    }
                )


            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("SearchUserViewModel", "search and select user error", t)
            }
        })
    }

    private fun getSearchByUserAndHost(): SearchByUserAndHost?{
        try{
            val ci = miCore.currentAccount.value?.getCurrentConnectionInformation()

            if(ci?.instanceBaseUrl == null){
                return null
            }
            if(mNowInstanceBase != ci.instanceBaseUrl){
                miCore.getMisskeyAPI(ci).let{ api ->
                    mNowInstanceBase = ci.instanceBaseUrl
                    mSearchByUserAndHost = SearchByUserAndHost(api)
                }
            }

            return mSearchByUserAndHost
        }catch(e: Exception){
            return null
        }

    }

    private fun getCi(): EncryptedConnectionInformation?{
        return miCore.currentAccount.value?.getCurrentConnectionInformation()
    }


}