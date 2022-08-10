package jp.panta.misskeyandroidclient.ui.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.util.task.asSuspend
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val accountViewDataFactory: AccountViewData.Factory,
    private val subscriptionUnRegistration: SubscriptionUnRegistration
) : ViewModel() {


    private val logger = loggerFactory.create("AccountViewModel")

    @FlowPreview
    val accounts = accountStore.observeAccounts.map { accounts ->
        accounts.map { ac ->
            accountViewDataFactory.create(ac, viewModelScope)
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
            userRepository
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
                subscriptionUnRegistration
                    .unregister(token, accountViewData.account.accountId)
            } catch (e: Throwable) {
                logger.warning("token解除処理失敗", e = e)
            }
            try {
                accountRepository.delete(accountViewData.account)
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