package jp.panta.misskeyandroidclient.viewmodel.auth.custom

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException

@Suppress("UNCHECKED_CAST")
class CustomAppCreatorViewModel(
    val connectionInstance: ConnectionInstance?,
    val misskeyAPI: MisskeyAPI?,
    val encryption: Encryption
) : ViewModel(){
    class Factory(val connectionInstance: ConnectionInstance?, val miApplication: MiApplication) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == CustomAppCreatorViewModel::class.java){
                return CustomAppCreatorViewModel(connectionInstance, miApplication.misskeyAPIService, miApplication.mEncryption) as T
            }
            throw IllegalArgumentException("use CustomAppCreatorViewModel::class.java")
        }
    }
    private val tag = "CustomAppCreator"

    val appName = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    val isCanBeCreated = MediatorLiveData<Boolean>().apply{
        addSource(appName){
            value = !it.isNullOrBlank()
        }
    }

    val app = MutableLiveData<App>()

    fun create(){
        val i = connectionInstance?.getI(encryption)
        if(i == null){
            return
        }
        if(isCanBeCreated.value == false){
            return
        }
        val obj = CreateApp(i = i,
            name = appName.value!!,
            description = description.value?: "",
            callbackUrl = "misskey://custom_auth_call_back",
            permission = CustomAppDefaultPermission.defaultPermission
        )

        misskeyAPI?.createApp(obj)?.enqueue(object : Callback<App>{
            override fun onResponse(call: Call<App>, response: Response<App>) {
                val tmpApp = response.body()
                if(tmpApp != null){
                    app.postValue(tmpApp)
                }else{
                    Log.d(tag, "作成に失敗した")
                }
            }

            override fun onFailure(call: Call<App>, t: Throwable) {
                Log.d(tag, "作成に失敗した", t)
            }
        })

    }
}