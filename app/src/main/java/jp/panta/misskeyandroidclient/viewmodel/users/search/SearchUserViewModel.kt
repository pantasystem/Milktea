package jp.panta.misskeyandroidclient.viewmodel.users.search

import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.SearchByUserAndHost
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * SearchAndSelectUserViewModelを将来的にこのSearchUserViewModelと
 * SelectedUserViewModelに分離する予定
 */
@FlowPreview
@ExperimentalCoroutinesApi
class SearchUserViewModel(
    val miCore: MiCore,
    val hasDetail: Boolean?,
    private val noteDataSourceAdder: NoteDataSourceAdder = NoteDataSourceAdder(miCore.getUserDataSource(), miCore.getNoteDataSource(), miCore.getFilePropertyDataSource())
) : ViewModel(){

    private val logger = miCore.loggerFactory.create("SearchUserViewModel")

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

    private val users = object : MediatorLiveData<List<UserViewData>>(){
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

        // TODO Service層などに切り出すなどをしてリファクタリングをする
        viewModelScope.launch(Dispatchers.IO) {
            val account = miCore.getAccountRepository().getCurrentAccount()
            isLoading.postValue(true)
            val resUsers = runCatching {
                getSearchByUserAndHost()?.search(request)?.body()
            }.onFailure {
                logger.error("request api/users/searchエラー", it)
            }.getOrNull()?.map {
                val u = it.toUser(account, isDetail = hasDetail?:false).also{ u ->
                    miCore.getUserDataSource().add(u)
                }
                it.pinnedNotes?.forEach { dto ->
                    noteDataSourceAdder.addNoteDtoToDataSource(account, dto)
                }
                UserViewData(u, miCore, viewModelScope)
            }?: emptyList()
            users.postValue(resUsers)
            isLoading.postValue(false)
        }

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