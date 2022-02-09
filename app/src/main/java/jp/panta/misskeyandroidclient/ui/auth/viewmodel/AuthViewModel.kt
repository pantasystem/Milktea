package jp.panta.misskeyandroidclient.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.auth.UserKey
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.account.newAccount
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import java.lang.IllegalStateException

@ExperimentalCoroutinesApi
class AuthViewModel(
    private val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(miCore) as T
        }
    }

    val error = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)

    private val mAuthorization = MutableStateFlow<Authorization>(Authorization.BeforeAuthentication)
    val authorization: StateFlow<Authorization> = mAuthorization

    init {
        authorization.flatMapLatest { a ->
            (0..Int.MAX_VALUE).asFlow().map {
                delay(4000)
                if(a is Authorization.Waiting4UserAuthorization) {
                    try {
                        val token = MisskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL)
                            .getAccessToken(UserKey(appSecret = a.appSecret, a.session.token))
                            .throwIfHasError()
                            .body()
                                ?: throw IllegalStateException("response bodyがありません。")

                        val authenticated = Authorization.Approved(
                            a.instanceBaseURL,
                            appSecret = a.appSecret,
                            token
                        )
                        State.Fixed(StateContent.Exist(authenticated))
                    }catch (e: Throwable) {
                        State.Error(StateContent.NotExist(), e)
                    }
                }else{
                    State.Fixed(StateContent.NotExist())
                }
            }
        }.mapNotNull {
            it.content as? StateContent.Exist
        }.onEach {
            setState(it.rawContent)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    /**
     * 認可されていることを前提にAuthTokenを取得しに行く
     */
    fun getAccessToken() {
        val a = authorization.value as? Authorization.Waiting4UserAuthorization
            ?: throw IllegalStateException("現在の状態: ${authorization.value}でアクセストークンを取得することはできません。")
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {

                MisskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL).getAccessToken(UserKey(appSecret = a.appSecret, a.session.token))
                    .throwIfHasError().body()
                    ?: throw IllegalStateException("response bodyがありません。")
            }.onSuccess {
                val authenticated = Authorization.Approved(
                    a.instanceBaseURL,
                    appSecret = a.appSecret,
                    it
                )
                setState(authenticated)
            }.onFailure {
                setState(Authorization.BeforeAuthentication)
                error.emit(it)
            }
        }
    }

    /**
     * 認可の確認
     */
    fun confirmApprove() {
        val a = authorization.value as? Authorization.Approved
            ?: throw IllegalStateException("confirmApproveは状態がApprovedの時以外呼び出すことはできません。")
        requireNotNull(a.appSecret)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().add(
                    a.accessToken.newAccount(a.instanceBaseURL, miCore.getEncryption(), a.appSecret),
                    false
                )
                val user = a.accessToken.user.toUser(account, true) as User.Detail
                miCore.getUserDataSource().add(user)
                miCore.getAccountRepository().add(account)
                miCore.setCurrentAccount(account)

                account to user
            }.onSuccess {
                setState(Authorization.Finish(it.first, it.second))
            }.onFailure {
                error.emit(it)
            }

        }
    }

    fun setState(state: Authorization) {
        mAuthorization.value = state
    }
}