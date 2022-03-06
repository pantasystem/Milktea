package jp.panta.misskeyandroidclient.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.mastodon.MastodonAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.misskey.auth.UserKey
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.api.misskey.users.toUser
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
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val miCore: MiCore,
    private val mastodonAPIProvider: MastodonAPIProvider,
    loggerFactory: Logger.Factory
) : ViewModel(){
    private val logger = loggerFactory.create("AuthViewModel")

    val error = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)

    private val mAuthorization = MutableStateFlow<Authorization>(Authorization.BeforeAuthentication)
    val authorization: StateFlow<Authorization> = mAuthorization

    init {
        authorization.flatMapLatest { a ->
            (0..Int.MAX_VALUE).asFlow().map {
                delay(4000)
                if(a is Authorization.Waiting4UserAuthorization.Misskey) {
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
    fun getAccessToken(code: String? = null) {
        val a = authorization.value as? Authorization.Waiting4UserAuthorization
            ?: throw IllegalStateException("現在の状態: ${authorization.value}でアクセストークンを取得することはできません。")
        viewModelScope.launch(Dispatchers.IO) {

            runCatching {
                when (a) {
                    is Authorization.Waiting4UserAuthorization.Misskey -> {
                        MisskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL).getAccessToken(UserKey(appSecret = a.appSecret, a.session.token))
                            .throwIfHasError().body()
                            ?: throw IllegalStateException("response bodyがありません。")
                    }
                    is Authorization.Waiting4UserAuthorization.Mastodon -> {
                        getAccessToken4Mastodon(a, code!!)
                        throw IllegalStateException("まだ未実装")
                    }
                }

            }.onSuccess {
                val authenticated = Authorization.Approved(
                    a.instanceBaseURL,
                    appSecret = (a as Authorization.Waiting4UserAuthorization.Misskey).appSecret,
                    it
                )
                setState(authenticated)
            }.onFailure {
                setState(Authorization.BeforeAuthentication)
                error.emit(it)
            }
        }
    }

    private suspend fun getAccessToken4Mastodon(
        a: Authorization.Waiting4UserAuthorization.Mastodon,
        code: String
    ): UserDTO {
        try {
            logger.debug("認証種別Mastodon: $a")
            val obtainToken = a.client.createObtainToken(scope = a.scope, code = code)
            val accessToken = mastodonAPIProvider.get(a.instanceBaseURL).obtainToken(obtainToken)
                .throwIfHasError()
                .body()
            logger.debug("accessToken:$accessToken")
            val me = mastodonAPIProvider.get(a.instanceBaseURL, accessToken!!.accessToken)
                .verifyCredentials()
                .throwIfHasError()
            return me.body()!!
        } catch (e: Exception) {
            logger.warning("AccessToken取得失敗", e = e)
            throw e
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