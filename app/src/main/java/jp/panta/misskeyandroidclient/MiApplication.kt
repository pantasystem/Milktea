package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.DataBase
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDao
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.collections.HashMap


//基本的な情報はここを返して扱われる
class MiApplication : Application(), MiCore {
    companion object{
        const val CURRENT_USER_ID = "jp.panta.misskeyandroidclient.MiApplication.CurrentUserId"
        const val TAG = "MiApplication"
    }

    /*var connectionInstanceDao: ConnectionInstanceDao? = null
        private set*/
    private lateinit var mAccountDao: AccountDao

    private lateinit var mNoteRequestSettingDao: NoteRequestSettingDao

    private lateinit var mConnectionInformationDao: ConnectionInformationDao

    lateinit var reactionHistoryDao: ReactionHistoryDao




    var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    /*var misskeyAPIService: MisskeyAPI? = null
        private set*/

    //private var misskeyAPIServiceDomainMap: Map<String, MisskeyAPI>? = null

    // private var mConnectionInstance: ConnectionInstance? = null

    override val accounts = MutableLiveData<List<AccountRelation>>()

    override val currentAccount = MutableLiveData<AccountRelation>()


    var isSuccessCurrentAccount = MutableLiveData<Boolean>()

    lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    private val mMisskeyAPIMap = HashMap<String, MisskeyAPI>()

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val database = Room.databaseBuilder(this, DataBase::class.java, "mi_database").fallbackToDestructiveMigration().build()
        //connectionInstanceDao = database.connectionInstanceDao()
        mAccountDao = database.accountDao()

        mNoteRequestSettingDao = database.noteSettingDao()

        mConnectionInformationDao = database.connectionInformationDao()

        reactionHistoryDao = database.reactionHistoryDao()

        mEncryption = KeyStoreSystemEncryption(this)


        GlobalScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                loadAndInitializeAccounts()
            }catch(e: Exception){
                isSuccessCurrentAccount.postValue(false)
            }
        }
    }

    override fun addAndChangeAccount(account: Account) {
        GlobalScope.launch(Dispatchers.IO){
            try{
                mAccountDao.insert(account)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.d(TAG, "add or change account error", e)
            }
        }
    }

    override fun logoutAccount(account: Account) {
        GlobalScope.launch(Dispatchers.IO){
            try{
                mAccountDao.delete(account)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "logout error", e)
            }
        }
    }



    override fun addPageInCurrentAccount(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mNoteRequestSettingDao.insert(noteRequestSetting)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun addAllPagesInCurrentAccount(noteRequestSettings: List<NoteRequest.Setting>){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mNoteRequestSettingDao.insertAll(noteRequestSettings)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removePageInCurrentAccount(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            try{
                noteRequestSetting.id?.let {
                    mNoteRequestSettingDao.delete(it)
                    loadAndInitializeAccounts()
                }
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removeAllPagesInCurrentAccount(noteRequestSettings: List<NoteRequest.Setting>){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mNoteRequestSettingDao.insertAll(noteRequestSettings)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun putConnectionInfoInCurrentAccount(ci: EncryptedConnectionInformation){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mConnectionInformationDao.insert(ci)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removeConnectSetting(connectionInformation: EncryptedConnectionInformation) {
        GlobalScope.launch(Dispatchers.IO){
            try{
                mConnectionInformationDao.delete(connectionInformation)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removeConnectionInfoInCurrentAccount(ci: EncryptedConnectionInformation){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mConnectionInformationDao.insert(ci)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    private fun loadAndInitializeAccounts(){
        try{
            val tmpAccounts = mAccountDao.findAllSetting()

            val current = tmpAccounts.firstOrNull {
                it.account.id == getCurrentUserId()
            }?: tmpAccounts.firstOrNull()


            isSuccessCurrentAccount.postValue(current?.getCurrentConnectionInformation() != null)

            current?.let{
                setCurrentUserId(it.account.id)
                val ci = it.getCurrentConnectionInformation()
                    ?:return

                loadInstanceMetaAndSetupAPI(ci)
            }?: return

            // setting networks

            currentAccount.postValue(current)
            accounts.postValue(tmpAccounts)

        }catch(e: Exception){
            isSuccessCurrentAccount.postValue(false)
            Log.e(TAG, "load and initialize error", e)
        }
    }


    private fun loadInstanceMetaAndSetupAPI(connectionInformation: EncryptedConnectionInformation): Meta?{
        val meta = mMetaInstanceUrlMap[connectionInformation.instanceBaseUrl]
            ?: getMisskeyAPI(connectionInformation).getMeta(RequestMeta()).execute().body()
        meta?.let{
            mMetaInstanceUrlMap[connectionInformation.instanceBaseUrl] = it
        }
        return meta
    }

    override fun getMisskeyAPI(ci: EncryptedConnectionInformation): MisskeyAPI{
        synchronized(mMisskeyAPIMap){
            val api = mMisskeyAPIMap[ci.instanceBaseUrl]
                ?: MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl)
            mMisskeyAPIMap[ci.instanceBaseUrl] = api
            return api
        }
    }

    override fun getEncryption(): Encryption {
        return mEncryption
    }
    @Deprecated("新データ構造移行に伴い使用禁止")
    fun addPageToNoteSettings(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            mNoteRequestSettingDao?.insert(noteRequestSetting)
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