package jp.panta.misskeyandroidclient.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.UserKey
import net.pantasystem.milktea.data.api.misskey.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.users.toUser
import net.pantasystem.milktea.data.model.account.newAccount
import net.pantasystem.milktea.data.model.auth.Authorization
import net.pantasystem.milktea.data.model.auth.custom.AccessToken
import net.pantasystem.milktea.data.model.auth.custom.toModel
import net.pantasystem.milktea.data.model.users.User
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
    loggerFactory: net.pantasystem.milktea.common.Logger.Factory
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
                            .getAccessToken(
                                net.pantasystem.milktea.api.misskey.auth.UserKey(
                                    appSecret = a.appSecret,
                                    a.session.token
                                )
                            )
                            .throwIfHasError()
                            .body()
                                ?: throw IllegalStateException("response bodyがありません。")

                        val authenticated = Authorization.Approved(
                            a.instanceBaseURL,
                            accessToken = token.toModel(a.appSecret)
                        )
                        net.pantasystem.milktea.common.State.Fixed(net.pantasystem.milktea.common.StateContent.Exist(authenticated))
                    }catch (e: Throwable) {
                        net.pantasystem.milktea.common.State.Error(net.pantasystem.milktea.common.StateContent.NotExist(), e)
                    }
                }else{
                    net.pantasystem.milktea.common.State.Fixed(net.pantasystem.milktea.common.StateContent.NotExist())
                }
            }
        }.mapNotNull {
            it.content as? net.pantasystem.milktea.common.StateContent.Exist
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
                        val accessToken = MisskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL).getAccessToken(
                            net.pantasystem.milktea.api.misskey.auth.UserKey(
                                appSecret = a.appSecret,
                                a.session.token
                            )
                        )
                            .throwIfHasError().body()
                            ?: throw IllegalStateException("response bodyがありません。")
                        accessToken.toModel(a.appSecret)
                    }
                    is Authorization.Waiting4UserAuthorization.Mastodon -> {
                        getAccessToken4Mastodon(a, code!!)
                    }
                }

            }.onSuccess {
                val authenticated = Authorization.Approved(
                    a.instanceBaseURL,
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
    ): AccessToken.Mastodon {
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
            logger.debug("自身の情報, code=${me.code()}, message=${me.message()}")
            val account = me.body()!!
            return accessToken.toModel(account)
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
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().add(
                    a.accessToken.newAccount(a.instanceBaseURL, miCore.getEncryption()),
                    false
                )
                val user = when(a.accessToken) {
                    is AccessToken.Mastodon -> {
                        (a.accessToken as AccessToken.Mastodon).account.toModel(account)
                    }
                    is AccessToken.Misskey -> {
                        (a.accessToken as AccessToken.Misskey).user.toUser(account, true) as User.Detail
                    }
                }
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