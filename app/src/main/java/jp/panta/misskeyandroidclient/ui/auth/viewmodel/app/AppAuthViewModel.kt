package jp.panta.misskeyandroidclient.ui.auth.viewmodel.app

import android.util.Log
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.panta.misskeyandroidclient.api.mastodon.MastodonAPIProvider
import jp.panta.misskeyandroidclient.api.mastodon.instance.Instance
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.misskey.app.CreateApp
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.auth.AppSecret
import jp.panta.misskeyandroidclient.api.misskey.auth.Session
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.*
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.Permissions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.IllegalArgumentException
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import jp.panta.misskeyandroidclient.api.mastodon.apps.CreateApp as CreateTootApp

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
    miCore: MiCore
) : ViewModel(){
    companion object{
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }


    private val urlPattern = Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    val instanceDomain = MutableStateFlow("")
    private val metaState = instanceDomain.flatMapLatest {
        getMeta(it)
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Lazily, State.Fixed(StateContent.NotExist()))

    private val _generatingTokenState = MutableStateFlow<State<Session>>(State.Fixed(StateContent.NotExist()))
    private val generatingTokenState: StateFlow<State<Session>> = _generatingTokenState
    private val generateTokenError = generatingTokenState.map {
        it as? State.Error
    }.map {
        it?.throwable
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val isFetchingMeta = metaState.map {
        it is State.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    private val fetchingMetaError = metaState.map {
        (it as? State.Error)?.throwable
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val isGeneratingToken = generatingTokenState.map {
        it is State.Loading
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isProgress = combine(isFetchingMeta, isGeneratingToken) { a, b ->
        a || b
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val validatedInstanceDomain = metaState.map {
        it is State.Fixed && it.content is StateContent.Exist
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val appName = MutableLiveData<String>()


    val app = MutableLiveData<AppType?>()
    val waiting4UserAuthorization = MutableLiveData<Authorization.Waiting4UserAuthorization?>()


    private val metaStore = miCore.getMetaStore()
    private val misskeyAPIProvider = miCore.getMisskeyAPIProvider()

    val errors = combine(generateTokenError, fetchingMetaError) { generateTokenError, fetchingMetaError ->
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

    private fun getMeta(instanceDomain: String): Flow<State<InstanceType>> {
        return flow<State<InstanceType>> {
            val url = toEnableUrl(instanceDomain)
            emit(State.Fixed(StateContent.NotExist()))
            if(urlPattern.matcher(url).find()){
                emit(State.Loading(StateContent.NotExist()))
                runCatching {
                    coroutineScope {
                        val misskey = async {
                            runCatching {
                                metaStore.fetch(url)
                            }.getOrNull()
                        }.await()
                        val mastodon = async {
                            runCatching {
                                mastodonAPIProvider.get(url)
                                    .getInstance()
                            }.getOrNull()

                        }.await()
                        if (misskey != null) {
                            return@coroutineScope InstanceType.Misskey(misskey)
                        }
                        if (mastodon != null) {
                            return@coroutineScope InstanceType.Mastodon(mastodon)
                        }
                        throw IllegalArgumentException()
                    }

                }.onFailure {
                    emit(State.Error(StateContent.NotExist(), it))
                }.onSuccess {
                    Log.d("AppAuthVM", "meta:$it")
                    emit(State.Fixed(
                        StateContent.Exist(it)
                    ))
                }
            }

        }

    }

    fun auth(){
        val url = this.instanceDomain.value
        val instanceBase = toEnableUrl(url)
        val appName = this.appName.value?: return
        val meta = (this.metaState.value.content as? StateContent.Exist)?.rawContent
            ?: return
        viewModelScope.launch(Dispatchers.IO){
            _generatingTokenState.value = State.Loading(generatingTokenState.value.content)
            runCatching {
                val app = createApp(instanceBase, meta, appName)
                this@AppAuthViewModel.app.postValue(app)
                when(app) {
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
                        val session = authApi.generateSession(AppSecret(secret!!)).body()
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
                if (w4a is Authorization.Waiting4UserAuthorization.Misskey) {
                    this@AppAuthViewModel.waiting4UserAuthorization.postValue(w4a)
                    _generatingTokenState.value = State.Fixed(StateContent.Exist(w4a.session))
                }

            }.onFailure {
                Log.e("AppAuthViewModel", "認証開始処理失敗", it)
                _generatingTokenState.value = State.Error(StateContent.NotExist(), it)
            }

        }


    }


    private suspend fun createApp(url: String, instanceType: InstanceType, appName: String): AppType {
        when (instanceType) {
            is InstanceType.Mastodon -> {
                val app = mastodonAPIProvider.get(url)
                    .createApp(CreateTootApp(
                        clientName = appName,
                        redirectUris = CALL_BACK_URL,
                        scopes = "read write"
                    )).throwIfHasError().body()
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

    private fun toEnableUrl(base: String) : String{
        var url = if(base.startsWith("https://")){
            base
        }else{
            "https://$base"
        }.replace(" ", "").replace("\t", "").replace("　", "")

        if(url.endsWith("/")) {
            url = url.substring(url.indices)
        }
        return url
    }

}