package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
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
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
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

    private lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    private val mMisskeyAPIMap = HashMap<String, MisskeyAPI>()

    private val mStreamingAccountMap = HashMap<Account, StreamingAdapter>()
    private val mMainCaptureAccountMap = HashMap<Account, MainCapture>()

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
                Log.e(TAG, "load account error", e)
                isSuccessCurrentAccount.postValue(false)
            }
        }
    }

    override fun switchAccount(account: Account) {
        GlobalScope.launch(Dispatchers.IO){
            try{

                setCurrentUserId(account.id)
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
                synchronized(mStreamingAccountMap){
                    val streaming = mStreamingAccountMap[account]
                    streaming?.disconnect()
                }
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "logout error", e)
            }
        }
    }



    override fun addPageInCurrentAccount(noteRequestSetting: NoteRequest.Setting){
        GlobalScope.launch(Dispatchers.IO){
            try{
                noteRequestSetting.accountId = currentAccount.value?.account?.id
                mNoteRequestSettingDao.insert(noteRequestSetting)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun replaceAllPagesInCurrentAccount(noteRequestSettings: List<NoteRequest.Setting>){
        GlobalScope.launch(Dispatchers.IO){
            try{
                noteRequestSettings.forEach{
                    it.accountId = currentAccount.value?.account?.id
                }
                currentAccount.value?.let {
                    mNoteRequestSettingDao.clearByAccount(it.account.id)
                    mNoteRequestSettingDao.insertAll(noteRequestSettings)
                    loadAndInitializeAccounts()
                }
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
                mNoteRequestSettingDao.deleteAll(noteRequestSettings)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun putConnectionInfo(account: Account, ci: EncryptedConnectionInformation){
        GlobalScope.launch(Dispatchers.IO){
            try{
                Log.d(TAG, "putConnectionInfo")
                val result = mAccountDao.insert(account)
                Log.d(this.javaClass.simpleName, "add account result:$result")
                //ci.accountId = account.id
                mConnectionInformationDao.add(ci)
                setCurrentUserId(account.id)
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

            current
                ?: Log.e(this.javaClass.simpleName, "load account error")
            Log.d(this.javaClass.simpleName, "load account relation result : $current")

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
        nowInstanceMeta = meta
        Log.d(TAG, "load meta result ${meta?.let{"成功"}?: "失敗"} ")

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

    override fun getMisskeyAPI(accountRelation: AccountRelation?): MisskeyAPI?{
        val ci = accountRelation?.getCurrentConnectionInformation()?: return null
        return getMisskeyAPI(ci)
    }

    override fun getEncryption(): Encryption {
        return mEncryption
    }

    private fun setCurrentUserId(userId: String){
        sharedPreferences.edit().apply{
            putString(CURRENT_USER_ID, userId)
        }.apply()

    }

    private fun getCurrentUserId(): String?{
        return sharedPreferences.getString(CURRENT_USER_ID, null)
    }

    fun getMainCapture(account: AccountRelation): MainCapture{
        val ci = account.getCurrentConnectionInformation()

        var streaming = synchronized(mStreamingAccountMap){
            mStreamingAccountMap[account.account]
        }

        val mainCapture = synchronized(mMainCaptureAccountMap){
            mMainCaptureAccountMap[account.account]
        }?: MainCapture(GsonFactory.create())

        if(streaming == null){
            streaming = StreamingAdapter(ci, getEncryption())
            streaming.addObserver(UUID.randomUUID().toString(), mainCapture)
            streaming.connect()

            synchronized(mStreamingAccountMap){
                mStreamingAccountMap[account.account] = streaming
            }
            synchronized(mMainCaptureAccountMap){
                mMainCaptureAccountMap[account.account] = mainCapture
            }
        }

        return mainCapture
    }



}