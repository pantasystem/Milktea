package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteRequestSettingDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.settings.ColorSettingStore
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.util.getPreferenceName
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

    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    lateinit var settingStore: SettingStore


    //private var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    /*var misskeyAPIService: MisskeyAPI? = null
        private set*/

    //private var misskeyAPIServiceDomainMap: Map<String, MisskeyAPI>? = null

    // private var mConnectionInstance: ConnectionInstance? = null

    override val accounts = MutableLiveData<List<AccountRelation>>()

    override val currentAccount = MutableLiveData<AccountRelation>()


    //var isSuccessCurrentAccount = MutableLiveData<Boolean>()
    var connectionStatus = MutableLiveData<ConnectionStatus>()

    private lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    private val mMisskeyAPIUrlMap = HashMap<String, Pair<Version?, MisskeyAPI>>()

    private val mStreamingAccountMap = HashMap<Account, StreamingAdapter>()
    private val mMainCaptureAccountMap = HashMap<Account, MainCapture>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)
        settingStore = SettingStore(sharedPreferences)

        val database = Room.databaseBuilder(this, DataBase::class.java, "mi_database")
            .addMigrations(MIGRATION_33_34)
            .addMigrations(MIGRATION_34_35)
            .addMigrations(MIGRATION_35_36)
            .addMigrations(MIGRATION_36_37)
            .build()
        //connectionInstanceDao = database.connectionInstanceDao()
        mAccountDao = database.accountDao()

        mNoteRequestSettingDao = database.noteSettingDao()

        mConnectionInformationDao = database.connectionInformationDao()

        reactionHistoryDao = database.reactionHistoryDao()

        reactionUserSettingDao = database.reactionUserSettingDao()

        mEncryption = KeyStoreSystemEncryption(this)


        GlobalScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "load account error", e)
                //isSuccessCurrentAccount.postValue(false)
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
            val tmpAccounts = try{
                mAccountDao.findAllSetting()

            }catch(e: Exception){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
                return
            }

            if(checkDirectSignInAccountAndDelete(tmpAccounts)){
                return loadAndInitializeAccounts()
            }

            if(checkCIEmptyAccountAndDelete(tmpAccounts)){
                return loadAndInitializeAccounts()
            }

            val current = tmpAccounts.firstOrNull {
                it.account.id == getCurrentUserId()
            }?: tmpAccounts.firstOrNull()

            current
                ?: Log.e(this.javaClass.simpleName, "load account error")
            Log.d(this.javaClass.simpleName, "load account relation result : $current")
            ConnectionStatus.ACCOUNT_ERROR

            //isSuccessCurrentAccount.postValue(current?.getCurrentConnectionInformation() != null)

            if(current == null){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
                return
            }

            val i = current.getCurrentConnectionInformation()?.getI(getEncryption())
            if(i == null){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
                return
            }

            val meta = loadInstanceMetaAndSetupAPI(current.getCurrentConnectionInformation()!!)

            if(meta == null){
                connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            }

            currentAccount.postValue(current)
            accounts.postValue(tmpAccounts)
            connectionStatus.postValue(ConnectionStatus.SUCCESS)

            setUpMetaMap(tmpAccounts)

        }catch(e: Exception){
            //isSuccessCurrentAccount.postValue(false)
            Log.e(TAG, "初期読み込みに失敗しまちた", e)
        }
    }

    private fun checkDirectSignInAccountAndDelete(accounts: List<AccountRelation>): Boolean{
        val directSignInAccounts = accounts.filter{
            it.connectionInformationList.any { ci ->
                ci.isDirect
            }
        }
        if(directSignInAccounts.isNotEmpty()){
            directSignInAccounts.forEach{
                it.connectionInformationList.forEach {  eci ->
                    try{
                        mConnectionInformationDao.delete(eci)
                    }catch(e: Exception){
                        Log.e("MiApplication", "アカウント削除中にエラー発生", e)
                    }
                }
            }
            return true
        }
        return false
    }

    private fun checkCIEmptyAccountAndDelete(accounts: List<AccountRelation>): Boolean{
        val emptyAccounts = accounts.filter{
            it.connectionInformationList.isEmpty()
        }
        if(emptyAccounts.isNotEmpty()){
            try{
                emptyAccounts.forEach{
                    mAccountDao.delete(it.account)
                }
            }catch(e: Exception){
                Log.e("MiApplication", "空アカウント削除中にエラー発生", e)
            }
            return true
        }
        return false
    }

    override fun getCurrentInstanceMeta(): Meta?{
        return synchronized(mMetaInstanceUrlMap){
            currentAccount.value?.getCurrentConnectionInformation()?.instanceBaseUrl?.let{ url ->
                mMetaInstanceUrlMap[url]
            }
        }
    }

    private fun setUpMetaMap(accounts: List<AccountRelation>){
        try{
            accounts.forEach{ ac ->
                ac.getCurrentConnectionInformation()?.let{ ci ->
                    loadInstanceMetaAndSetupAPI(ci)
                }
            }
        }catch(e: Exception){
            Log.e(TAG, "meta取得中にエラー発生", e)
        }
    }


    private fun loadInstanceMetaAndSetupAPI(connectionInformation: EncryptedConnectionInformation): Meta?{
        try{
            val meta = synchronized(mMisskeyAPIUrlMap){
                try{
                    mMetaInstanceUrlMap[connectionInformation.instanceBaseUrl]
                }catch(e: Exception){
                    Log.d(TAG, "metaマップからの取得に失敗したでち")
                    null
                }
            } ?: try{
                MisskeyGetMeta.getMeta(connectionInformation.instanceBaseUrl).execute().body()
            }catch(e: Exception){
                Log.d(TAG, "metaをオンラインから取得するのに失敗したでち")
                connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)

                null
            }


            Log.d(TAG, "load meta result ${meta?.let{"成功"}?: "失敗"} ")

            meta?: return null

            synchronized(mMetaInstanceUrlMap){
                mMetaInstanceUrlMap[connectionInformation.instanceBaseUrl] = meta
            }
            synchronized(mMisskeyAPIUrlMap){
                val versionAndApi = mMisskeyAPIUrlMap[connectionInformation.instanceBaseUrl]
                if(versionAndApi?.first != meta.getVersion()){
                    val newApi = MisskeyAPIServiceBuilder.build(connectionInformation.instanceBaseUrl, meta.getVersion())
                    mMisskeyAPIUrlMap[connectionInformation.instanceBaseUrl] = Pair(meta.getVersion(), newApi)
                }
            }
            return meta

        }catch(e: Exception){
            Log.e(TAG, "metaの読み込み一連処理に失敗したでち", e)
            connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            return null
        }


    }

    override fun getMisskeyAPI(ci: EncryptedConnectionInformation): MisskeyAPI{
        synchronized(mMisskeyAPIUrlMap){
            val api = mMisskeyAPIUrlMap[ci.instanceBaseUrl]
                ?: Pair(null, MisskeyAPIServiceBuilder.build(ci.instanceBaseUrl))
            mMisskeyAPIUrlMap[ci.instanceBaseUrl] = api
            return api.second
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

    override fun getMainCapture(account: AccountRelation): MainCapture{
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