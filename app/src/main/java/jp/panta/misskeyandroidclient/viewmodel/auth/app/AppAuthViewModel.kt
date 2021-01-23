package jp.panta.misskeyandroidclient.viewmodel.auth.app

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.viewmodel.auth.DefaultPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.util.*
import java.util.regex.Pattern

@Suppress("UNCHECKED_CAST")
class AppAuthViewModel(
    val customAuthStore: CustomAuthStore
) : ViewModel(){
    companion object{
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }

    class Factory(val customAuthStore: CustomAuthStore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppAuthViewModel(customAuthStore) as T
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
    val session = MutableLiveData<Session?>()

    val generatingToken = MutableLiveData<Boolean>(false)

    init{
        meta.addSource(instanceDomain){ base ->
            val url = if(base.startsWith("https://")){
                base
            }else{
                "https://$base"
            }.replace(" ", "").replace("\t", "").replace("　", "")
            if(urlPattern.matcher(url).find()){

                try{
                    MisskeyGetMeta.getMeta(url).enqueue(object : Callback<Meta>{
                        override fun onResponse(call: Call<Meta>, response: Response<Meta>) {
                            meta.postValue(response.body())
                        }

                        override fun onFailure(call: Call<Meta>, t: Throwable) {
                            meta.postValue(null)
                        }
                    })
                }catch(e: IllegalArgumentException){
                    Log.w("AppAuthViewModel", "不正なURLの可能性があります", e)
                }
            }

        }
        validatedInstanceDomain.addSource(meta){
            validatedInstanceDomain.value = it != null
        }
    }


    fun startAuth(){
        generatingToken.value = true
        val url = this.instanceDomain.value?: return
        val instanceBase = "https://$url"
        val appName = this.appName.value?: return
        val meta = this.meta.value?: return
        val misskeyAPI = MisskeyAPIServiceBuilder.build(instanceBase, meta.getVersion())
        viewModelScope.launch(Dispatchers.IO){
            try{
                val app = misskeyAPI.createApp(
                    CreateApp(
                        null,
                        appName,
                        "misskey android application",
                        CALL_BACK_URL,
                        permission = DefaultPermission.defaultPermission
                    )
                ).execute()?.body()
                Log.d("AppAuthViewModel", "created app: $app")
                this@AppAuthViewModel.app.postValue(app)
                app?: return@launch
                val secret = app.secret
                val authApi = MisskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
                val session = authApi.generateSession(AppSecret(secret!!)).execute().body()
                this@AppAuthViewModel.session.postValue(session)
                session?: return@launch

                customAuthStore.setCustomAuthBridge(
                    CustomAuthBridge(
                        secret = secret,
                        instanceDomain = instanceBase,
                        session = session,
                        enabledDateEnd = Date(Date().time + 60 * 1000 * 2),
                        viaName = appName

                    )
                )

            }catch(e: Exception){
                generatingToken.postValue(false)
            }finally{
                generatingToken.postValue(false)
            }


        }


    }


}