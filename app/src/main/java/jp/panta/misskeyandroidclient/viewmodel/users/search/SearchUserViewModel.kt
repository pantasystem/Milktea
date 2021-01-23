package jp.panta.misskeyandroidclient.viewmodel.users.search

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.SearchByUserAndHost
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UsersLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

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

    private val compositeDisposable = CompositeDisposable()

    private val searchUserRequests = PublishSubject.create<RequestUser>()
    val userName = MutableLiveData<String>()
    val host = MutableLiveData<String>()

    val isLoading = MutableLiveData<Boolean>()

    private val users = object : UsersLiveData(){
        override fun onActive() {
            super.onActive()

            addRequestSubscriber()
        }

        override fun onInactive() {
            super.onInactive()
            compositeDisposable.clear()

        }
    }.apply{
        addSource(userName){
            search()
        }
        addSource(host){
            search()
        }
        addSource(miCore.getCurrentAccount()){
            setMainCapture(miCore.getMainCapture(it))
        }
    }

    private var mSearchByUserAndHost: SearchByUserAndHost? = null
    private var mNowInstanceBase: String? = null


    fun getUsers(): LiveData<List<UserViewData>> {
        return users
    }

    fun search(){
        val userName = this.userName.value?: return
        val host = this.host.value

        val request = RequestUser(
            i = getAccount()?.getI(miCore.getEncryption())!!,
            userName = userName,
            userId = null,
            host = host,
            detail = hasDetail
        )
        searchUserRequests.onNext(request)
    }
    fun search(request: RequestUser){


        isLoading.postValue(true)
        getSearchByUserAndHost()?.search(request)?.enqueue(object : Callback<List<UserDTO>> {
            override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                users.postValue(
                    response.body()?.map{
                        UserViewData(it)
                    }
                )
                isLoading.postValue(false)


            }

            override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                Log.e("SearchUserViewModel", "search and select user error", t)
                isLoading.postValue(false)
            }
        })
    }

    private fun getSearchByUserAndHost(): SearchByUserAndHost?{
        try{
            val account = miCore.getCurrentAccount().value

            if(account?.instanceDomain == null){
                return null
            }
            if(mNowInstanceBase != account.instanceDomain){
                miCore.getMisskeyAPI(account).let{ api ->
                    mNowInstanceBase = account.instanceDomain
                    mSearchByUserAndHost = SearchByUserAndHost(api)
                }
            }

            return mSearchByUserAndHost
        }catch(e: Exception){
            return null
        }

    }

    private fun getAccount(): Account?{
        return miCore.getCurrentAccount().value
    }

    private fun addRequestSubscriber(){
        val disposable = searchUserRequests.distinctUntilChanged()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribe {
                search(it)
            }
        compositeDisposable.add(disposable)
    }

}