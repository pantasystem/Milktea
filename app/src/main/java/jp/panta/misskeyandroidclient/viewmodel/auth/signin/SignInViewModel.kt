package jp.panta.misskeyandroidclient.viewmodel.auth.signin

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException


class SignInViewModel(
    val connectionInstances: MutableLiveData<List<ConnectionInstance>>,
    val encryption: Encryption,
    val mode: Int
) : ViewModel(){
    companion object{
        private const val TAG = "SignInViewModel"
        const val MODE_OVERWRITE = 0
        const val MODE_ADD = 1
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(val miApplication: MiApplication, val mode: Int) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == SignInViewModel::class.java){
                return SignInViewModel(miApplication.connectionInstancesLiveData, miApplication.mEncryption, mode) as T
            }
            throw IllegalArgumentException("use SignInViewModel::class.java")
        }
    }

    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val isValidDomain = MutableLiveData<Boolean>(false)

    var misskeyAPI: MisskeyAPI? = null

    val connectionInstance = MutableLiveData<ConnectionInstance>()

    val instanceDomain = MediatorLiveData<String>()

    //val i = MutableLiveData<String>()
    val isValidityOfAuth = MutableLiveData<Boolean>(false)

    //val me = MutableLiveData<User?>()

    init{
        instanceDomain.observeForever {
            try{
                misskeyAPI = MisskeyAPIServiceBuilder.build(it)
            }catch(e: Exception){
                isValidDomain.postValue(false)
            }
            misskeyAPI?.getMeta(RequestMeta(false))?.enqueue(object : Callback<Meta?>{
                override fun onResponse(call: Call<Meta?>, response: Response<Meta?>) {
                    isValidDomain.postValue(
                        response.code() == 200
                    )
                }
                override fun onFailure(call: Call<Meta?>, t: Throwable) {
                    isValidDomain.postValue(false)
                }
            })
        }
    }

    fun signIn(){
        val un = userName.value
        val pw = password.value
        val domain = instanceDomain.value?: return
        if(un != null && pw != null){
            misskeyAPI?.signIn(SignIn(
                username = un,
                password = pw)
            )?.enqueue(object : Callback<I>{
                override fun onResponse(call: Call<I>, response: Response<I>) {
                    val res = response.body()
                    if(res == null){
                        isValidityOfAuth.postValue(false)
                    }else{
                        isValidityOfAuth.postValue(true)
                        //i.postValue(res.i)
                        Log.d(TAG, "認証に成功しました")
                        makeConnectionInstance(domain = domain, i = res)
                    }
                }

                override fun onFailure(call: Call<I>, t: Throwable) {
                    isValidityOfAuth.postValue(false)
                }
            })
        }
    }


    private fun makeConnectionInstance(domain: String, i: I){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val res = misskeyAPI?.i(i)?.execute()
                if(res == null){
                    Log.e(TAG, "Retrofitが初期されていない")
                    return@launch
                }
                val user = res.body()
                if(user == null){
                    Log.d(TAG, "api/iの取得に失敗した, code:${res.code()}, error:${res.errorBody()?.string()}")
                    return@launch
                }

                val connectionInstance = if(mode == MODE_OVERWRITE){
                    ConnectionInstance(instanceBaseUrl = domain, userId = user.id).apply{
                        setDirectI(i.i, encryption)
                    }
                }else{
                    connectionInstances.value?.firstOrNull {ex ->
                        ex.userId == user.id
                    }?.apply{
                        val exState = state
                        setDirectI(i.i, encryption)
                        state = exState
                    }?: ConnectionInstance(instanceBaseUrl = domain, userId = user.id).apply{
                        setDirectI(i.i, encryption)
                    }
                }
                this@SignInViewModel.connectionInstance.postValue(connectionInstance)
            }catch(e: Exception){
                Log.e(TAG, "ConnectionInstance作成中にエラーが発生しました", e)
            }

        }

    }

}