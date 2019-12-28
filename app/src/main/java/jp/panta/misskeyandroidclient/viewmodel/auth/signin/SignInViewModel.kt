package jp.panta.misskeyandroidclient.viewmodel.auth.signin

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException


class SignInViewModel(
    val connectionInstanceDao: ConnectionInstanceDao,
    val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val connectionInstanceDao: ConnectionInstanceDao, val encryption: Encryption) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == SignInViewModel::class.java){
                return SignInViewModel(connectionInstanceDao, encryption) as T
            }
            throw IllegalArgumentException("use SignInViewModel::class.java")
        }
    }

    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val isValidDomain = MutableLiveData<Boolean>(false)

    var misskeyAPI: MisskeyAPI? = null

    val instanceDomain = MediatorLiveData<String>().apply{
        addSource(this){
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

    val i = MutableLiveData<String>()
    val isValidityOfAuth = MutableLiveData<Boolean>(false)

    val me = MutableLiveData<User?>()

    fun signIn(){
        val un = userName.value
        val pw = password.value
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
                        i.postValue(res.i)
                        loadMeAndAdd()
                    }
                }

                override fun onFailure(call: Call<I>, t: Throwable) {
                    isValidityOfAuth.postValue(false)
                }
            })
        }
    }

    private fun loadMeAndAdd(){
        val i =  i.value?: return
        misskeyAPI?.i(I(i))?.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                if(user == null){
                    me.postValue(null)
                }else{
                    val ci = ConnectionInstance(user.id, instanceDomain.value!!).apply{
                        setDirectI(i, encryption)
                    }
                    viewModelScope.launch(Dispatchers.IO){
                        connectionInstanceDao.insert(ci)
                    }
                    me.postValue(user)

                }
            }
            override fun onFailure(call: Call<User>, t: Throwable) {
                me.postValue(null)
            }
        })
    }

}