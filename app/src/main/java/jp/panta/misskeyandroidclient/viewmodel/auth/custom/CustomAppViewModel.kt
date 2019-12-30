package jp.panta.misskeyandroidclient.viewmodel.auth.custom

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthBridge
import jp.panta.misskeyandroidclient.model.auth.custom.CustomAuthStore
import jp.panta.misskeyandroidclient.model.auth.custom.ShowApp
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.util.*

@Suppress("UNCHECKED_CAST")
class CustomAppViewModel(
    val currentConnectionInstanceLiveData: MutableLiveData<ConnectionInstance>,
    val accounts: MutableLiveData<List<User>>,
    val encryption: Encryption,
    var misskeyAPI: MisskeyAPI,
    val customAuthStore: CustomAuthStore
) : ViewModel(){

    class Factory(
        val miApplication: MiApplication
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == CustomAppViewModel::class.java){
                return CustomAppViewModel(
                    miApplication.currentConnectionInstanceLiveData,
                    miApplication.accountsLiveData,
                    miApplication.encryption,
                    miApplication.misskeyAPIService!!,
                    CustomAuthStore.newInstance(miApplication)
                ) as T
            }
            throw IllegalArgumentException("use CustomAppViewModel::class.java")
        }
    }
    val tag = "CustomAppViewModel"
    //val accounts =
    val selectedApp = MutableLiveData<App>()
    val apps = MutableLiveData<List<App>>()
    val account = Transformations.map(currentConnectionInstanceLiveData){ci ->
        loadApps()
        val user = accounts.value?.firstOrNull {
            it.id == ci.userId
        }
        if(user == null){
            null
        }else{
            AccountViewData(user, ci)
        }
    }

    val isSignInRequiredEvent = EventBus<Boolean>()

    val createAppEvent = EventBus<Unit>()

    val startChoosingAppEvent = EventBus<Unit>()

    val appSelectedEvent = EventBus<App>()

    val session = MutableLiveData<Session>()

    val isCanAuthenticated = MediatorLiveData<Boolean>()

    init{
        isCanAuthenticated.addSource(selectedApp){
            isCanAuthenticated.value = it != null && account.value != null
        }
        isCanAuthenticated.addSource(account){
            isCanAuthenticated.value = selectedApp.value != null && it != null
        }

        currentConnectionInstanceLiveData.observeForever {
            Log.d(tag, "directI:${it.getDirectI(encryption)}")
            isSignInRequiredEvent.event = it.getDirectI(encryption) == null
        }
    }


    private fun loadApps(){
        val nowCn = currentConnectionInstanceLiveData.value?: return
        val nowI = nowCn.getI(encryption)?: return
        misskeyAPI.myApps(I(nowI)).enqueue(object : Callback<List<App>>{
            override fun onResponse(call: Call<List<App>>, response: Response<List<App>>) {
                val resBody = response.body()
                if(resBody != null){
                    apps.postValue(resBody.filter{
                        it.callbackUrl == "misskey://custom_auth_call_back"
                                && CustomAppDefaultPermission.defaultPermission.all {a ->
                            it.permission.any{b ->
                                a == b
                            }
                        }
                    })
                    Log.d(tag, "apps読み込みに成功 domain:${nowCn.instanceBaseUrl}, i:$nowI")
                    if(nowCn.state == ConnectionInstance.CUSTOM_APP){
                        val selected = resBody.firstOrNull {
                            it.secret == nowCn.getCustomAppSecret(encryption)
                        }?: return

                        selectedApp.postValue(selected)
                    }
                }else{
                    Log.d(tag, "apps読み込みに失敗, code:${response.code()}, domain:${nowCn.instanceBaseUrl}, i:$nowI, errorMsg: ${call.request().url().toString()}")
                }

            }

            override fun onFailure(call: Call<List<App>>, t: Throwable) {
                Log.d(tag, "apps読み込みに失敗しました, errorMsg",t)
            }
        })
    }

    fun setApp(id: String){
        val nowI = currentConnectionInstanceLiveData.value?.getI(encryption)?: return
        misskeyAPI.myApps(I(nowI)).enqueue(object : Callback<List<App>>{
            override fun onResponse(call: Call<List<App>>, response: Response<List<App>>) {
                val list = response.body()?: return
                apps.postValue(list.filter{
                    it.callbackUrl == "misskey://custom_auth_call_back"
                            && CustomAppDefaultPermission.defaultPermission.all {a ->
                        it.permission.any{b ->
                            a == b
                        }
                    }
                })
                val selected = list.firstOrNull {
                    it.id == id
                }?: return

                selectedApp.postValue(selected)
            }

            override fun onFailure(call: Call<List<App>>, t: Throwable) {
                Log.d(tag, "apps読み込みに失敗", t)
            }
        })
    }

    fun createApp(){
        createAppEvent.event = Unit
    }

    fun startChoosingApp(){
        startChoosingAppEvent.event = Unit
    }

    fun chooseApp(app: App){
        selectedApp.value = app
        appSelectedEvent.event = app
    }

    fun authenticate(){

        viewModelScope.launch(Dispatchers.IO) {
            try{
                val app = selectedApp.value?: return@launch
                val ci = currentConnectionInstanceLiveData.value?: return@launch
                val i  = ci.getDirectI(encryption)
                if(i == null){
                    isSignInRequiredEvent.event = true
                    return@launch
                }

                val secret =
                    if (app.secret != null){
                        app.secret
                    }else{
                        val res = misskeyAPI.showApp(ShowApp(i = i, appId = app.id)).execute()
                        Log.d(tag, "code:${res.code()}, secret is null:${res.body()?.secret == null}")
                        res.body()?.secret
                    }

                secret?: return@launch
                val api = MisskeyAPIServiceBuilder.buildAuthAPI(ci.instanceBaseUrl)
                api.generateSession(AppSecret(appSecret = secret)).enqueue(object : Callback<Session>{
                    override fun onResponse(call: Call<Session>, response: Response<Session>) {
                        val body = response.body()
                        Log.d(tag, "受信:$body")
                        if(body == null){
                            Log.d(tag, "失敗しましたSessionがNull")
                            return
                        }
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.MINUTE, 3)
                        val bridge = CustomAuthBridge(
                            secret = secret,
                            instanceDomain = ci.instanceBaseUrl,
                            session = body,
                            enabledDateEnd = calendar.time
                        )
                        customAuthStore.setCustomAuthBridge(bridge)
                        session.postValue(response.body())
                    }

                    override fun onFailure(call: Call<Session>, t: Throwable) {
                        Log.e(tag, "error", t)
                    }
                })

            }catch(e: Exception){
                Log.e(tag, "認証中にエラー", e)
            }
        }
    }
}