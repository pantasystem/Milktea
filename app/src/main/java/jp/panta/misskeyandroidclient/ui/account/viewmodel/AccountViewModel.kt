package jp.panta.misskeyandroidclient.ui.account.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.util.task.asSuspend
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
@HiltViewModel
class AccountViewModel @Inject constructor(
    val miCore: MiCore,
    val accountStore: AccountStore,
    val userDataSource: UserDataSource,
) : ViewModel() {

    companion object {
        const val TAG = "AccountViewModel"
    }

    private val logger = miCore.loggerFactory.create("AccountViewModel")

    @OptIn(ExperimentalCoroutinesApi::class)
    @FlowPreview
    val accounts = accountStore.observeAccounts.map { accounts ->
        accounts.map { ac ->
            AccountViewData(ac, miCore, viewModelScope, Dispatchers.IO)
        }
    }.catch { e ->
        logger.debug("アカウントロードエラー", e = e)
    }.asLiveData()

    val currentAccount =
        accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val user = currentAccount.flatMapLatest { account ->
        userDataSource.state.map { state ->
            account?.let {
                state.get(User.Id(account.accountId, account.remoteId))
            }
        }.map {
            it as? User.Detail
        }
    }.asLiveData()

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<User.Id>()
    val showFollowings = EventBus<User.Id>()

    val showProfile = EventBus<Account>()

    val switchTargetConnectionInstanceEvent = EventBus<Unit>()

    init {
        accountStore.observeCurrentAccount.filterNotNull().onEach { ac ->
            miCore.getUserRepository()
                .find(User.Id(ac.accountId, ac.remoteId), true)
        }.catch { e ->
            logger.error("現在のアカウントの取得に失敗した", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun setSwitchTargetConnectionInstance(account: Account) {
        switchTargetConnectionInstanceEvent.event = Unit
        viewModelScope.launch(Dispatchers.IO) {
            accountStore.setCurrent(account)
        }
    }

    fun showSwitchDialog() {
        switchAccount.event = switchAccount.event
    }

    fun showFollowers(userId: User.Id?) {
        userId?.let {
            showFollowers.event = userId
        }
    }

    fun showFollowings(userId: User.Id?) {
        userId?.let {
            showFollowings.event = userId
        }
    }

    fun showProfile(account: Account?) {
        if (account == null) {
            logger.debug("showProfile account未取得のためキャンセル")
            return
        }
        showProfile.event = account
    }

    @FlowPreview
    fun signOut(accountViewData: AccountViewData) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = FirebaseMessaging.getInstance().token.asSuspend()
                miCore.getSubscriptionUnRegstration()
                    .unregister(token, accountViewData.account.accountId)
            } catch (e: Throwable) {
                logger.warning("token解除処理失敗", e = e)
            }
            try {
                miCore.getAccountRepository().delete(accountViewData.account)
            } catch (e: Throwable) {
                logger.error("ログアウト処理失敗", e)
            }
            logger.info("ログアウト処理成功")

        }
    }

    fun addPage(page: Page) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                accountStore.addPage(page)
            } catch (e: Throwable) {
                logger.error("pageの追加に失敗", e = e)
            }
        }
    }

    fun removePage(page: Page) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                accountStore.removePage(page)
            } catch (e: Throwable) {
                logger.error("pageの削除に失敗", e = e)
            }
        }
    }

}