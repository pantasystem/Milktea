package jp.panta.misskeyandroidclient.viewmodel.auth.app

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.app.CreateApp
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.api.auth.AppSecret
import jp.panta.misskeyandroidclient.api.auth.Session
import jp.panta.misskeyandroidclient.model.auth.Authorization
import jp.panta.misskeyandroidclient.model.auth.custom.*
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.auth.DefaultPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST")
class AppAuthViewModel(
    val customAuthStore: CustomAuthStore,
    miCore: MiCore
) : ViewModel(){
    companion object{
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }

    class Factory(val customAuthStore: CustomAuthStore, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppAuthViewModel(customAuthStore, miCore) as T
        }
    }

    private val urlPattern = Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    val instanceDomain = MutableLiveData<String>()
    val meta = MediatorLiveData<Meta?>()

    val validatedInstanceDomain = MediatorLiveData<Boolean>()

    val appName = MutableLiveData<String>()
    val validatedAppName = Transformations.map(appName){
        it.isNotBlank()
    }

    val app = MutableLiveData<App?>()
    val waiting4UserAuthorization = MutableLiveData<Authorization.Waiting4UserAuthorization?>()

    val generatingToken = MutableLiveData<Boolean>(false)

    private val metaStore = miCore.getMetaStore()
    private val misskeyAPIProvider = miCore.getMisskeyAPIProvider()

    init{
        meta.addSource(instanceDomain){ base ->
            val url = if(base.startsWith("https://")){
                base
            }else{
                "https://$base"
            }.replace(" ", "").replace("\t", "").replace("　", "")
            if(urlPattern.matcher(url).find()){

                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        metaStore.get(url)
                    }.onFailure {
                        meta.postValue(null)
                    }.onSuccess {
                        meta.postValue(it)
                    }
                }
            }
        }
        validatedInstanceDomain.addSource(meta){
            validatedInstanceDomain.value = it != null
        }
    }


    fun auth(){
        generatingToken.value = true
        val url = this.instanceDomain.value?: return
        val instanceBase = "https://$url"
        val appName = this.appName.value?: return
        val meta = this.meta.value?: return
        viewModelScope.launch(Dispatchers.IO){
            runCatching {
                val app = createApp(instanceBase, meta.getVersion(), appName)
                this@AppAuthViewModel.app.postValue(app)
                val secret = app.secret
                val authApi = MisskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
                val session = authApi.generateSession(AppSecret(secret!!)).execute().body()
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

            }.onFailure {
                Log.e("AppAuthViewModel", "認証開始処理失敗", it)
            }.also {
                generatingToken.postValue(false)
            }

        }


    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun createApp(url: String, version: Version, appName: String): App {
        val misskeyAPI = misskeyAPIProvider.get(url, version)
        return misskeyAPI.createApp(
            CreateApp(
                null,
                appName,
                "misskey android application",
                CALL_BACK_URL,
                permission = DefaultPermission.defaultPermission
            )
        ).execute().throwIfHasError().body()
            ?: throw IllegalStateException("Appの作成に失敗しました。")
    }


}