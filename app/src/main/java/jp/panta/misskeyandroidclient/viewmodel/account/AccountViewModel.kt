package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.util.task.asSuspend
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class AccountViewModel(
    val miCore: MiCore
) : ViewModel(){

    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass == AccountViewModel::class.java){
                return AccountViewModel(miCore) as T
            }
            throw IllegalArgumentException("use AccountViewModel::class.java")
        }
    }

    companion object{
        const val TAG = "AccountViewModel"
    }

    private val logger = miCore.loggerFactory.create("AccountViewModel")

    @FlowPreview
    val accounts = MediatorLiveData<List<AccountViewData>>().apply{
        miCore.getAccounts().onEach { accounts ->
            val viewDataList = accounts.map{ ac ->
                AccountViewData(ac, miCore, viewModelScope, Dispatchers.IO)
            }
            postValue(viewDataList)
        }.catch { e ->
            logger.debug("アカウントロードエラー", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    val currentAccount = miCore.getCurrentAccount()

    val user = MediatorLiveData<User.Detail>()

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<User.Id>()
    val showFollowings = EventBus<User.Id>()

    val showProfile = EventBus<Account>()

    val switchTargetConnectionInstanceEvent = EventBus<Unit>()

    init{
        miCore.getCurrentAccount().filterNotNull().map { ac ->
            miCore.getUserRepository().find(User.Id(ac.accountId, ac.remoteId), true) as? User.Detail
        }.filterNotNull().map { user ->
            user
        }.onEach {
            user.postValue(it)
        }.catch { e ->
            logger.error("現在のアカウントの取得に失敗した", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

        miCore.getCurrentAccount().filterNotNull().flatMapLatest { ac->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map {
                ac to it
            }
        }.map { pair ->
            (pair.second as? ChannelBody.Main.MeUpdated)?.let{ meUpdated ->
                pair.first to meUpdated
            }
        }.filterNotNull().onEach {
            val user = it.second.body.toUser(it.first, true)
            miCore.getUserDataSource().add(user)
            this.user.postValue(user as User.Detail)
        }.catch { e ->
            logger.error("MeUpdated取得エラー", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun setSwitchTargetConnectionInstance(account: Account){
        switchTargetConnectionInstanceEvent.event = Unit
        viewModelScope.launch(Dispatchers.IO) {
            miCore.setCurrentAccount(account)
        }
    }

    fun showSwitchDialog(){
        switchAccount.event = switchAccount.event
    }

    fun showFollowers(userId: User.Id?){
        userId?.let {
            showFollowers.event = userId
        }
    }

    fun showFollowings(userId: User.Id?){
        userId?.let{
            showFollowings.event = userId
        }
    }

    fun showProfile(account: Account?){
        account?: return
        showProfile.event = account
    }

    @FlowPreview
    fun signOut(accountViewData: AccountViewData){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val token = FirebaseMessaging.getInstance().token.asSuspend()
                miCore.getSubscriptionUnRegstration().unregister(token, accountViewData.account.accountId)
            }catch(e: Throwable) {
                logger.warning("token解除処理失敗", e = e)
            }
            try {
                miCore.getAccountRepository().delete(accountViewData.account)
            }catch(e: Throwable) {
                logger.error("ログアウト処理失敗", e)
            }
            logger.info("ログアウト処理成功")

        }
    }


}