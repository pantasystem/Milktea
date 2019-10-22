package jp.panta.misskeyandroidclient.viewmodel.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.SecretConstant
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Instance
import jp.panta.misskeyandroidclient.model.auth.Session
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModel : ViewModel(){

    val instances = SecretConstant.getInstances().map{
        it.value
    }
    val currentInstance = MutableLiveData<Instance>(instances.firstOrNull())

    var misskeyAPI = MisskeyAPIServiceBuilder.buildAuthAPI("https://misskey.io")

    val sessionLiveData = MutableLiveData<Session>()

    val errorMessage = MutableLiveData<String>()

    fun selectInstance(instance: Instance){
        currentInstance.value = instance
        misskeyAPI = MisskeyAPIServiceBuilder.buildAuthAPI(instance.domain)
    }

    fun startAuth(){
        val instance = currentInstance.value
        if(instance != null){
            misskeyAPI.generateSession(AppSecret(instance.appSecret)).enqueue(object :Callback<Session>{
                override fun onResponse(call: Call<Session>, response: Response<Session>) {
                    sessionLiveData.postValue(response.body())
                }

                override fun onFailure(call: Call<Session>, t: Throwable) {
                    errorMessage.postValue("セッションの取得に失敗しました")
                }
            })
        }
    }

}