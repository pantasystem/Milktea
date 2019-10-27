package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


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
    var currentConnectionInstanceLiveData = MutableLiveData<ConnectionInstance>()

    var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    var misskeyAPIService: MisskeyAPI? = null
        private set

    var streamingAdapter: StreamingAdapter? = null
        private set
    val noteCapture: NoteCapture = NoteCapture(null)

    val timelineCapture: TimelineCapture = TimelineCapture()

    //var connectionInstance: ConnectionInstance? = null
    private var mConnectionInstance: ConnectionInstance? = null

    var isSuccessLoadConnectionInstance = MutableLiveData<Boolean>()
    //val streamingAdapter: StreamingAdapter = StreamingAdapter(getConnectionInstance())

    /*fun getConnectionInstance(): ConnectionInstance{
        return ConnectionInstance(instanceBaseUrl = nowInstance, userId = "7roinhytrr", accessToken = "")
    }*/



    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val database = Room.databaseBuilder(this, DataBase::class.java, "mi_database").fallbackToDestructiveMigration().build()
        connectionInstanceDao = database.connectionInstanceDao()

        noteRequestSettingDao = database.noteSettingDao()

        val currentUserId = getCurrentUserId()
        GlobalScope.launch{
            try{
                val ci = if(currentUserId == null){
                    connectionInstanceDao!!.findAll()?.firstOrNull()

                }else{
                    connectionInstanceDao!!.findByUserId(currentUserId)
                }

                if(ci == null){
                    Log.w("MiApplication", "接続可能なアカウントを発見不能")
                    isSuccessLoadConnectionInstance.postValue(false)
                }else{
                    //init
                    initConnectionInstance(ci)
                }
            }catch(e: Exception){
                isSuccessLoadConnectionInstance.postValue(false)

            }

        }


    }

    fun setCurrentInstance(ci: ConnectionInstance){
        this.currentConnectionInstanceLiveData.postValue(ci)
        initConnectionInstance(ci)

    }

    private fun initConnectionInstance(ci: ConnectionInstance){
        try{
            setCurrentUserId(ci.userId)
            misskeyAPIService = MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl)
            streamingAdapter = StreamingAdapter(ci)
            streamingAdapter?.connect()
            //noteCapture = NoteCapture(ci.userId)
            noteCapture.myUserId = ci.userId
            //timelineCapture = TimelineCapture()

            streamingAdapter?.addObserver(noteCapture)
            streamingAdapter?.addObserver(timelineCapture)

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
}