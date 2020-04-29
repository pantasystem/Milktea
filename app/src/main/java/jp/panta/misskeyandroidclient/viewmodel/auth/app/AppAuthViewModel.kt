package jp.panta.misskeyandroidclient.viewmodel.auth.app

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.viewmodel.auth.DefaultPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AppAuthViewModel(
    val customAuthStore: CustomAuthStore
) : ViewModel(){
    companion object{
        const val CALL_BACK_URL = "misskey://app_auth_callback"
    }

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
            MisskeyGetMeta.getMeta(base).enqueue(object : Callback<Meta>{
                override fun onResponse(call: Call<Meta>, response: Response<Meta>) {
                    meta.postValue(response.body())
                }

                override fun onFailure(call: Call<Meta>, t: Throwable) {
                    meta.postValue(null)
                }
            })
        }
        validatedInstanceDomain.addSource(meta){
            validatedInstanceDomain.value = it != null
        }
    }


    fun startAuth(){
        generatingToken.value = true
        val instanceBase = this.instanceDomain.value?: return
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