package jp.panta.misskeyandroidclient.viewmodel

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MiViewModel(application: Application) : AndroidViewModel(application){
    companion object{
        const val CURRENT_USER_ID = "jp.panta.misskeyandroidclient.MiViewModel.CurrentUserId"
    }

    val currentConnectionInstance = MutableLiveData<ConnectionInstance>()
    val connectionInstances = MutableLiveData<List<ConnectionInstance>>()

    val isSuccessLoadConnectionInstance = MutableLiveData<Boolean>()

    val relationConnectionInstancePropertiesVersion = MutableLiveData<Int>(0)
    var misskeyAPIService: MisskeyAPI? = null
        private set

    private val nowInstanceMeta = MutableLiveData<Meta>()

    var streamingAdapter: StreamingAdapter? = null
        private set
    val noteCapture: NoteCapture = NoteCapture(null)

    val timelineCapture: TimelineCapture = TimelineCapture()

    private val database = Room.databaseBuilder(application, DataBase::class.java, "mi_database").fallbackToDestructiveMigration().build()
    private val connectionInstanceDao = database.connectionInstanceDao()

    val noteRequestSettingDao = database.noteSettingDao()

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    init{

        initLoadConnectionInstance()
    }

    fun switchAccount(ci: ConnectionInstance){
        viewModelScope.launch(Dispatchers.IO){
            try{
                updateRelationConnectionInstanceProperty(ci)
            }catch(e: Exception){
                Log.d("MiViewModel", "アカウントの切り替えに失敗しました")
            }
        }
    }

    fun addAccount(ci: ConnectionInstance){
        viewModelScope.launch(Dispatchers.IO){
            try{
                connectionInstanceDao.insert(ci)
                updateRelationConnectionInstanceProperty(ci)
            }catch(e: Exception){
                Log.e("MiViewModel", "アカウントを追加することに失敗しました", e)
            }
        }
    }

    private fun initLoadConnectionInstance(){
        val currentUser = sharedPreferences.getString(CURRENT_USER_ID, null)

        viewModelScope.launch(Dispatchers.IO){
            try{
                val connectionInstances = connectionInstanceDao.findAll()
                this@MiViewModel.connectionInstances.postValue(connectionInstances)

                val current = if(currentUser == null){
                    connectionInstances?.firstOrNull()
                }else{
                    connectionInstances?.find {
                                        it.userId == currentUser
                                    } ?: connectionInstances?.firstOrNull()
                }

                if(current == null){
                    Log.w("MiViewModel", "接続可能なアカウントを発見不能")

                    isSuccessLoadConnectionInstance.postValue(false)
                    return@launch
                }else{
                    updateRelationConnectionInstanceProperty(current)
                }

            }catch(e: Exception){
                isSuccessLoadConnectionInstance.postValue(false)
            }

        }

    }

    private fun updateRelationConnectionInstanceProperty(ci: ConnectionInstance){
        setCurrentUserId(ci.userId)

        misskeyAPIService = MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl)
        streamingAdapter = StreamingAdapter(ci)
        streamingAdapter?.connect()

        noteCapture.myUserId = ci.userId

        streamingAdapter?.apply{
            /*addObserver(noteCapture)
            addObserver(timelineCapture)*/
        }

        isSuccessLoadConnectionInstance.postValue(true)
        currentConnectionInstance.postValue(ci)

        updateMeta(ci)
        val oldVersion = relationConnectionInstancePropertiesVersion.value?: 0
        relationConnectionInstancePropertiesVersion.postValue(oldVersion + 1)
    }

    private fun setCurrentUserId(userId: String){
        sharedPreferences.edit().apply{
            putString(MiApplication.CURRENT_USER_ID, userId)
        }.apply()

    }

    private fun updateMeta(ci: ConnectionInstance){
        misskeyAPIService?.getMeta(RequestMeta())
            ?.enqueue(object : Callback<Meta> {
                override fun onResponse(call: Call<Meta>, response: Response<Meta>) {
                    nowInstanceMeta.postValue(response.body())
                    Log.d("MiApplication", "$nowInstanceMeta")
                }
                override fun onFailure(call: Call<Meta>, t: Throwable) {
                    Log.w("MiApplication", "metaの取得に失敗した", t)
                }
            })
    }
}