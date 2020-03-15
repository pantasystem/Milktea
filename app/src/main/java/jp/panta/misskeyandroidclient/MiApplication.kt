package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountDao
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.core.AccountStore
import jp.panta.misskeyandroidclient.viewmodel.core.ConnectionInformationStore
import jp.panta.misskeyandroidclient.viewmodel.notes.NotePageStore
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException


//基本的な情報はここを返して扱われる
class MiApplication : Application(), AccountStore, ConnectionInformationStore, NotePageStore{
    companion object{
        const val CURRENT_USER_ID = "jp.panta.misskeyandroidclient.MiApplication.CurrentUserId"
    }

    /*var connectionInstanceDao: ConnectionInstanceDao? = null
        private set*/
    private var mAccountDao: AccountDao? = null

    var noteRequestSettingDao: NoteRequestSettingDao? = null

    lateinit var reactionHistoryDao: ReactionHistoryDao

    override val currentAccount: LiveData<AccountRelation> = MutableLiveData<AccountRelation>()

    override val accounts: LiveData<List<AccountRelation>> = MutableLiveData<List<AccountRelation>>()

    //var currentUserId: String? = null

    //private val misskeyAPIService = MisskeyAPIServiceBuilder.build(nowInstance)
    //val currentConnectionInstanceLiveData = MutableLiveData<ConnectionInstance>()

    //val connectionInstancesLiveData = MutableLiveData<List<ConnectionInstance>>()

    //val currentAccountLiveData = MutableLiveData<User>()
    //val accountsLiveData = MutableLiveData<List<User>>()

    var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    var misskeyAPIService: MisskeyAPI? = null
        private set

    //private var misskeyAPIServiceDomainMap: Map<String, MisskeyAPI>? = null

    private var mConnectionInstance: ConnectionInstance? = null

    var isSuccessLoadConnectionInstance = MutableLiveData<Boolean>()

    lateinit var encryption: Encryption


    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val database = Room.databaseBuilder(this, DataBase::class.java, "mi_database").fallbackToDestructiveMigration().build()
        //connectionInstanceDao = database.connectionInstanceDao()
        mAccountDao = database.accountDao()

        noteRequestSettingDao = database.noteSettingDao()

        reactionHistoryDao = database.reactionHistoryDao()

        encryption = KeyStoreSystemEncryption(this)

        val currentUserId = getCurrentUserId()

        GlobalScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                val accountList = mAccountDao?.findAllSetting()
                (accounts as MutableLiveData).postValue(accountList?: emptyList())
                //this@MiApplication.connectionInstancesLiveData.postValue(connectionInstances)
                Log.d("MiApplication", "accounts: ${accounts.value}")



                val current = if(currentUserId == null){
                    accountList?.firstOrNull()
                }else{
                    mAccountDao?.findSettingByAccountId(currentUserId)
                }

                if(current == null){
                    Log.w("MiApplication", "接続可能なアカウントを発見不能")
                    isSuccessLoadConnectionInstance.postValue(false)
                }else{
                    //updateRelationConnectionInstanceProperty(current)
                }
            }catch(e: Exception){
                isSuccessLoadConnectionInstance.postValue(false)
            }
        }





    }

    override fun add(account: Account) {
        GlobalScope.launch(Dispatchers.IO){
            mAccountDao?.insert(account)

        }
    }

    override fun remove(account: Account) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeCurrent(account: Account) {

    }




    fun addPageToNoteSettings(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            noteRequestSettingDao?.insert(noteRequestSetting)
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



}