package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.SearchByUserAndHost
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchAndSelectUserViewModel(
    val accountRelation: AccountRelation,
    val misskeyAPI: MisskeyAPI,
    val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountRelation: AccountRelation, val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val misskeyAPI = miCore.getMisskeyAPI(accountRelation)!!
            return SearchAndSelectUserViewModel(accountRelation, misskeyAPI, miCore.getEncryption()) as T
        }
    }

    companion object{
        private const val TAG = "SearchAndSelectUserVM"
    }

    private val mSearchByUserAndHost = SearchByUserAndHost(misskeyAPI)

    val userName = MutableLiveData<String>()
    val host = MutableLiveData<String>()

    val searchResultUsers = MediatorLiveData<List<User>>()

    val selectedUser = MutableLiveData<User>()

    init{
        searchResultUsers.addSource(userName){
            search()
        }
        searchResultUsers.addSource(host){
            search()
        }
    }

    fun search(){
        val userName = this.userName.value?: return
        val host = this.host.value

        val request = RequestUser(
            i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
            userName = userName,
            userId = null,
            host = host
        )

        mSearchByUserAndHost.search(request).enqueue(object : Callback<List<User>>{
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                searchResultUsers.postValue(
                    response.body()
                )
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(TAG, "search and select user error", t)
            }
        })
    }

    fun select(user: User?){
        selectedUser.value = user
    }
}