package jp.panta.misskeyandroidclient.ui.auth.viewmodel.app

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.misskey.app.CreateApp
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.api.misskey.auth.AppSecret
import jp.panta.misskeyandroidclient.api.misskey.auth.Session
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.*
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.auth.viewmodel.Permissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*
import java.util.regex.Pattern


sealed interface AuthErrors {
    val throwable: Throwable
    data class GetMetaError(
        override val throwable: Throwable,
    ) : AuthErrors

    data class GenerateTokenError(
        override val throwable: Throwable
    ) : AuthErrors
}

@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class AppAuthViewModel(
    private val customAuthStore: CustomAuthStore,
    miCore: MiCore
) : ViewModel(){
    companion object{
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }

    class Factory(private val customAuthStore: CustomAuthStore, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppAuthViewModel(customAuthStore, miCore) as T
        }
    }

    private val urlPattern = Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    val instanceDomain = MutableStateFlow("")
    //private val _metaState = MutableStateFlow<State<Meta>>(State.Fixed(StateContent.NotExist()))
    //private val metaState: StateFlow<State<Meta>> = _metaState
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


    val app = MutableLiveData<App?>()
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

    private fun getMeta(instanceDomain: String): Flow<State<Meta>> {
        return flow<State<Meta>> {
            val url = toEnableUrl(instanceDomain)
            emit(State.Fixed(StateContent.NotExist()))
            if(urlPattern.matcher(url).find()){
                emit(State.Loading(StateContent.NotExist()))
                runCatching {
                    metaStore.fetch(url)
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
                val app = createApp(instanceBase, meta.getVersion(), appName)
                this@AppAuthViewModel.app.postValue(app)
                val secret = app.secret
                val authApi = MisskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
                val session = authApi.generateSession(AppSecret(secret!!)).body()
                    ?: throw IllegalStateException("セッションの作成に失敗しました。")
                customAuthStore.setCustomAuthBridge(
                    app.createAuth(instanceBase, session)
                )
                Authorization.Waiting4UserAuthorization(
                    instanceBase,
                    appSecret = app.secret,
                    session = session,
                    viaName = app.name
                )
            }.onSuccess { w4a ->
                this@AppAuthViewModel.waiting4UserAuthorization.postValue(w4a)
                _generatingTokenState.value = State.Fixed(StateContent.Exist(w4a.session))

            }.onFailure {
                Log.e("AppAuthViewModel", "認証開始処理失敗", it)
                _generatingTokenState.value = State.Error(StateContent.NotExist(), it)
            }

        }


    }


    private suspend fun createApp(url: String, version: Version, appName: String): App {
        val misskeyAPI = misskeyAPIProvider.get(url, version)
        return misskeyAPI.createApp(
            CreateApp(
                null,
                appName,
                "misskey android application",
                CALL_BACK_URL,
                permission = Permissions.getPermission(version)
            )
        ).throwIfHasError().body()
            ?: throw IllegalStateException("Appの作成に失敗しました。")
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