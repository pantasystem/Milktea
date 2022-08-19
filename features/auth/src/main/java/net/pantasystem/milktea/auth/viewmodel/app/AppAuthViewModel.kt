package net.pantasystem.milktea.auth.viewmodel.app

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.mastodon.instance.Instance
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.app.CreateApp
import net.pantasystem.milktea.api.misskey.auth.AppSecret
import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.api.misskey.auth.fromDTO
import net.pantasystem.milktea.auth.viewmodel.Permissions
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.custom.createAuth
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.Meta
import java.util.regex.Pattern
import javax.inject.Inject
import net.pantasystem.milktea.api.mastodon.apps.CreateApp as CreateTootApp

sealed interface AuthErrors {
    val throwable: Throwable

    data class GetMetaError(
        override val throwable: Throwable,
    ) : AuthErrors

    data class GenerateTokenError(
        override val throwable: Throwable
    ) : AuthErrors
}

sealed interface InstanceType {
    data class Mastodon(val instance: Instance) : InstanceType
    data class Misskey(val instance: Meta) : InstanceType
}


@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
@HiltViewModel
class AppAuthViewModel @Inject constructor(
    private val customAuthStore: CustomAuthStore,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val metaStore: FetchMeta,
    loggerFactory: Logger.Factory,
    accountRepository: AccountRepository,
) : ViewModel() {
    companion object {
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }

    private val logger = loggerFactory.create("AppAuthViewModel")

    private val urlPattern =
        Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    val instanceDomain = MutableStateFlow("")
    private val metaState = instanceDomain.flatMapLatest {
        getMeta(it)
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope, SharingStarted.Lazily, ResultState.Fixed(
            StateContent.NotExist()
        )
    )

    private val _generatingTokenState = MutableStateFlow<ResultState<Session>>(
        ResultState.Fixed(StateContent.NotExist())
    )
    private val generatingTokenState: StateFlow<ResultState<Session>> = _generatingTokenState
    private val generateTokenError = generatingTokenState.map {
        it as? ResultState.Error
    }.map {
        it?.throwable
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val isFetchingMeta = metaState.map {
        it is ResultState.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val fetchingMetaError = metaState.map {
        (it as? ResultState.Error)?.throwable
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val isGeneratingToken = generatingTokenState.map {
        it is ResultState.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isProgress = combine(isFetchingMeta, isGeneratingToken) { a, b ->
        a || b
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val validatedInstanceDomain = metaState.map {
        it is ResultState.Fixed && it.content is StateContent.Exist
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val appName = MutableLiveData<String>()


    val app = MutableLiveData<AppType?>()
    val waiting4UserAuthorization = MutableLiveData<Authorization.Waiting4UserAuthorization?>()


    val errors =
        combine(generateTokenError, fetchingMetaError) { generateTokenError, fetchingMetaError ->
            when {
                generateTokenError != null -> {
                    AuthErrors.GenerateTokenError(generateTokenError)
                }
                fetchingMetaError != null -> {
                    AuthErrors.GetMetaError(fetchingMetaError)
                }
                else -> {
                    null
                }
            }
        }

    init {
        // NOTE: misskey.ioにログインしているアカウントが一つもなければmisskey.ioをデフォルト表示する
        // NOTE: またioにログインしていた場合は空にする
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.findAll().onSuccess { accounts ->
                if (!accounts.any { it.getHost() == "misskey.io" }) {
                    instanceDomain.value = "misskey.io"
                }
            }.onFailure {
                logger.error("findAll accounts failure", it)
            }
        }
    }

    fun clearHostName() {
        instanceDomain.value = ""
    }

    private fun getMeta(instanceDomain: String): Flow<ResultState<InstanceType>> {
        return flow {
            val url = toEnableUrl(instanceDomain)
            emit(ResultState.Fixed(StateContent.NotExist()))
            if (urlPattern.matcher(url).find()) {
                emit(ResultState.Loading(StateContent.NotExist()))
                runCatching {
                    coroutineScope {
                        val misskey = withContext(Dispatchers.IO) {
                            runCatching {
                                metaStore.fetch(url)
                            }.onFailure {
                                Log.e("AppAuthViewModel", "fetch meta error", it)
                            }.getOrNull()
                        }
                        val mastodon =
                            withContext(Dispatchers.IO) {
                                if (!BuildConfig.DEBUG) {
                                    return@withContext null
                                }
                                runCatching {
                                    mastodonAPIProvider.get(url)
                                        .getInstance()
                                }.getOrNull()
                            }
                        if (misskey != null) {
                            return@coroutineScope InstanceType.Misskey(misskey)
                        }
                        if (mastodon != null) {
                            return@coroutineScope InstanceType.Mastodon(mastodon)
                        }
                        throw IllegalArgumentException()
                    }

                }.onFailure {
                    emit(ResultState.Error(StateContent.NotExist(), it))
                }.onSuccess {
                    Log.d("AppAuthVM", "meta:$it")
                    emit(
                        ResultState.Fixed(
                            StateContent.Exist(it)
                        )
                    )
                }
            }

        }

    }

    fun auth() {
        val url = this.instanceDomain.value
        val instanceBase = toEnableUrl(url)
        val appName = this.appName.value ?: return
        val meta = (this.metaState.value.content as? StateContent.Exist)?.rawContent
            ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _generatingTokenState.value = ResultState.Loading(generatingTokenState.value.content)
            runCatching {
                val app = createApp(instanceBase, meta, appName)
                this@AppAuthViewModel.app.postValue(app)
                when (app) {
                    is AppType.Mastodon -> {
                        val authState = app.createAuth(instanceBase, "read write")
                        customAuthStore.setCustomAuthBridge(authState)
                        Authorization.Waiting4UserAuthorization.Mastodon(
                            instanceBase,
                            client = app,
                            scope = "read write"
                        )
                    }
                    is AppType.Misskey -> {
                        val secret = app.secret
                        val authApi = MisskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
                        val session = authApi.generateSession(
                            AppSecret(
                                secret!!
                            )
                        ).body()
                            ?: throw IllegalStateException("セッションの作成に失敗しました。")
                        customAuthStore.setCustomAuthBridge(
                            app.createAuth(instanceBase, session)
                        )
                        Authorization.Waiting4UserAuthorization.Misskey(
                            instanceBase,
                            appSecret = app.secret!!,
                            session = session,
                            viaName = app.name
                        )
                    }
                }


            }.onSuccess { w4a ->
                this@AppAuthViewModel.waiting4UserAuthorization.postValue(w4a)
                if (w4a is Authorization.Waiting4UserAuthorization.Misskey) {
                    _generatingTokenState.value = ResultState.Fixed(StateContent.Exist(w4a.session))
                }
            }.onFailure {
                Log.e("AppAuthViewModel", "認証開始処理失敗", it)
                _generatingTokenState.value = ResultState.Error(StateContent.NotExist(), it)
            }

        }


    }


    private suspend fun createApp(
        url: String,
        instanceType: InstanceType,
        appName: String
    ): AppType {
        when (instanceType) {
            is InstanceType.Mastodon -> {
                val app = mastodonAPIProvider.get(url)
                    .createApp(
                        CreateTootApp(
                            clientName = appName,
                            redirectUris = CALL_BACK_URL,
                            scopes = "read write"
                        )
                    ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromDTO(app)
            }
            is InstanceType.Misskey -> {
                val version = instanceType.instance.getVersion()
                val misskeyAPI = misskeyAPIProvider.get(url, version)
                val app = misskeyAPI.createApp(
                    CreateApp(
                        null,
                        appName,
                        "misskey android application",
                        CALL_BACK_URL,
                        permission = Permissions.getPermission(version)
                    )
                ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromDTO(app)
            }
        }

    }

    private fun toEnableUrl(base: String): String {
        var url = if (base.startsWith("https://")) {
            base
        } else {
            "https://$base"
        }.replace(" ", "").replace("\t", "").replace("　", "")

        if (url.endsWith("/")) {
            url = url.substring(url.indices)
        }
        return url
    }

}