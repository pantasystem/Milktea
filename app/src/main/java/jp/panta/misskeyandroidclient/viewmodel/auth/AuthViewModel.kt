package jp.panta.misskeyandroidclient.viewmodel.auth

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
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.IllegalStateException

class AuthViewModel(
    private val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AuthViewModel(miCore) as T
        }
    }

    val error = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)

    private val mAuthorization = MutableStateFlow<Authorization>(Authorization.BeforeAuthentication)
    val authorization: StateFlow<Authorization> = mAuthorization


    /**
     * 認可されていることを前提にAuthTokenを取得しに行く
     */
    fun getAccessToken() {
        val a = authorization.value as? Authorization.Waiting4UserAuthorization
            ?: throw IllegalStateException("現在の状態: ${authorization.value}でアクセストークンを取得することはできません。")
        requireNotNull( a.app.secret)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {

                MisskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL).getAccessToken(UserKey(appSecret = a.app.secret, a.session.token))
                    .execute().throwIfHasError().body()
                    ?: throw IllegalStateException("response bodyがありません。")
            }.onSuccess {
                val authenticated = Authorization.Approved(
                    a.instanceBaseURL,
                    a.app,
                    it
                )
                setState(authenticated)
            }.onFailure {
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
        requireNotNull(a.app.secret)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().add(
                    a.accessToken.newAccount(a.instanceBaseURL, miCore.getEncryption(), a.app.secret),
                    false
                )
                val user = a.accessToken.user.toUser(account, true) as User.Detail
                miCore.getUserDataSource().add(user)
                account to user
            }.onSuccess {
                miCore.addAccount(it.first)
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