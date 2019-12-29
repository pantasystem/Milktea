package jp.panta.misskeyandroidclient.viewmodel.auth.custom

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class CustomAppViewModel(
    val currentConnectionInstanceLiveData: MutableLiveData<ConnectionInstance>,
    val connectionInstanceDao: ConnectionInstanceDao,
    val accounts: MutableLiveData<List<User>>,
    val encryption: Encryption,
    var misskeyAPI: MisskeyAPI
) : ViewModel(){

    class Factory(
        val miApplication: MiApplication
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == CustomAppViewModel::class.java){
                return CustomAppViewModel(
                    miApplication.currentConnectionInstanceLiveData,
                    miApplication.connectionInstanceDao!!,
                    miApplication.accountsLiveData,
                    miApplication.encryption,
                    miApplication.misskeyAPIService!!
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

    val createAppEvent = EventBus<Unit>()

    val startChoosingAppEvent = EventBus<Unit>()

    val appSelectedEvent = EventBus<App>()


    private fun loadApps(){
        val nowCn = currentConnectionInstanceLiveData.value?: return
        val nowI = nowCn.getI(encryption)?: return
        misskeyAPI.myApps(I(nowI)).enqueue(object : Callback<List<App>>{
            override fun onResponse(call: Call<List<App>>, response: Response<List<App>>) {
                val resBody = response.body()
                if(resBody != null){
                    apps.postValue(resBody)
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
                apps.postValue(list)
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

    }
}