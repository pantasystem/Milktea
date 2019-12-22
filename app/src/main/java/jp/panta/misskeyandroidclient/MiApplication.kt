package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException


//基本的な情報はここを返して扱われる
class MiApplication : Application(){
    companion object{
        const val CURRENT_USER_ID = "jp.panta.misskeyandroidclient.MiApplication.CurrentUserId"
    }

    var connectionInstanceDao: ConnectionInstanceDao? = null
        private set

    var noteRequestSettingDao: NoteRequestSettingDao? = null
    //var currentUserId: String? = null

    //private val misskeyAPIService = MisskeyAPIServiceBuilder.build(nowInstance)
    val currentConnectionInstanceLiveData = MutableLiveData<ConnectionInstance>()

    val connectionInstancesLiveData = MutableLiveData<List<ConnectionInstance>>()

    val currentAccountLiveData = MutableLiveData<User>()
    val accountsLiveData = MutableLiveData<List<User>>()

    var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    var misskeyAPIService: MisskeyAPI? = null
        private set

    private var misskeyAPIServiceDomainMap: Map<String, MisskeyAPI>? = null

    private var mConnectionInstance: ConnectionInstance? = null

    var isSuccessLoadConnectionInstance = MutableLiveData<Boolean>()


    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val database = Room.databaseBuilder(this, DataBase::class.java, "mi_database").fallbackToDestructiveMigration().build()
        connectionInstanceDao = database.connectionInstanceDao()

        noteRequestSettingDao = database.noteSettingDao()

        val currentUserId = getCurrentUserId()

        GlobalScope.launch{
            try{
                val connectionInstances = connectionInstanceDao!!.findAll()
                this@MiApplication.connectionInstancesLiveData.postValue(connectionInstances)
                Log.d("MiApplication", "connectionInstances: $connectionInstances")

                misskeyAPIServiceDomainMap = connectionInstances?.map{
                    it.instanceBaseUrl to MisskeyAPIServiceBuilder.build(it.instanceBaseUrl)
                }?.toMap()

                val current = if(currentUserId == null){
                    connectionInstanceDao?.findAll()?.firstOrNull()
                }else{
                    connectionInstanceDao!!.findByUserId(currentUserId)
                        ?: connectionInstanceDao?.findAll()?.firstOrNull()
                }

                if(current == null){
                    Log.w("MiApplication", "接続可能なアカウントを発見不能")
                    isSuccessLoadConnectionInstance.postValue(false)
                }else{
                    updateRelationConnectionInstanceProperty(current)
                }
            }catch(e: Exception){
                isSuccessLoadConnectionInstance.postValue(false)
            }
        }

        connectionInstancesLiveData.observeForever {
            updateAccounts(it)
        }

        currentConnectionInstanceLiveData.observeForever {
            updateCurrentAccount(it, misskeyAPIService)
        }



    }

    fun switchAccount(ci: ConnectionInstance){

        val count = connectionInstancesLiveData.value?.filter{
            it.userId == ci.userId && it.accessToken == ci.accessToken
        }?.size
        if(count != null && count > 0){
            updateRelationConnectionInstanceProperty(ci)
        }else{
            throw IllegalArgumentException("対象のアカウントが存在しませんこのメソッドは無効です")
        }

    }

    fun addAccount(ci: ConnectionInstance){
        GlobalScope.launch(Dispatchers.IO){
            try{
                connectionInstanceDao?.insert(ci)
                val connectionInstances = connectionInstanceDao?.findAll()
                misskeyAPIServiceDomainMap = connectionInstances?.map{
                    it.instanceBaseUrl to MisskeyAPIServiceBuilder.build(it.instanceBaseUrl)
                }?.toMap()

                updateRelationConnectionInstanceProperty(ci)
                connectionInstancesLiveData.postValue(connectionInstanceDao?.findAll())
            }catch(e: Exception){
                Log.e("MiViewModel", "アカウントを追加することに失敗しました", e)
            }
        }
    }


    fun addPageToNoteSettings(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            noteRequestSettingDao?.insert(noteRequestSetting)
        }
    }

    private fun updateRelationConnectionInstanceProperty(ci: ConnectionInstance){
        try{
            setCurrentUserId(ci.userId)
            //misskeyAPIService = MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl)
            var service = misskeyAPIServiceDomainMap?.get(ci.instanceBaseUrl)
            if(service == null){
                Log.e("MiApplication","MisskeyAPIServiceを発見できなかった")
                service = MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl)
            }
            misskeyAPIService = service

            mConnectionInstance = ci
            currentConnectionInstanceLiveData.postValue(ci)
            setMeta()
            isSuccessLoadConnectionInstance.postValue(true)
        }catch(e: Exception){
            Log.e("MiApplication", "初期化中に致命的なエラー", e)
            isSuccessLoadConnectionInstance.postValue(false)
        }

    }

    private fun setCurrentUserId(userId: String){
        sharedPreferences.edit().apply{
            putString(CURRENT_USER_ID, userId)
        }.apply()

    }

    private fun getCurrentUserId(): String?{
        return sharedPreferences.getString(CURRENT_USER_ID, null)
    }

    private fun setMeta(){
        misskeyAPIService?.getMeta(RequestMeta())
            ?.enqueue(object : Callback<Meta>{
                override fun onResponse(call: Call<Meta>, response: Response<Meta>) {
                    nowInstanceMeta = response.body()
                    Log.d("MiApplication", "$nowInstanceMeta")
                }
                override fun onFailure(call: Call<Meta>, t: Throwable) {
                    Log.w("MiApplication", "metaの取得に失敗した", t)
                }
            })
    }

    private fun updateAccounts(instances: List<ConnectionInstance>){

        val a = instances.map{
            GlobalScope.async {
                try{
                    val api = misskeyAPIServiceDomainMap?.get(it.instanceBaseUrl)

                    api?.i(I(it.getI()!!))?.execute()?.body()

                }catch(e: Exception){
                    null
                }
            }
        }
        GlobalScope.launch{
            try{
                Log.d("MiApplication", "Accountsの取得を開始します")
                val notNullUsers = a.awaitAll().filterNotNull()
                accountsLiveData.postValue(notNullUsers)
            }catch(e: Exception){
                Log.d("MiApplication", "Account取得中にエラー発生", e)
            }

        }

    }

    private fun updateCurrentAccount(ci: ConnectionInstance, misskeyAPI: MisskeyAPI?){
        misskeyAPI?.i(I(i = ci.getI()!!))?.enqueue(object : Callback<User>{
            override fun onResponse(call: Call<User>, response: Response<User>) {
                Log.d("MiApplication", "iを取得しました${response.body()}")
                currentAccountLiveData.postValue(response.body())
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.d("MiApplication", "iの取得に失敗しました")
            }
        })
    }

}