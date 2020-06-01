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
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.settings.ColorSettingStore
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streming.MainCapture
import jp.panta.misskeyandroidclient.model.streming.Observer
import jp.panta.misskeyandroidclient.model.streming.StreamingAdapter
import jp.panta.misskeyandroidclient.model.streming.TimelineCapture
import jp.panta.misskeyandroidclient.model.streming.note.NoteCapture
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.model.messaging.MessageSubscriber
import jp.panta.misskeyandroidclient.model.url.JSoupUrlPreviewStore
import jp.panta.misskeyandroidclient.model.url.UrlPreviewStore
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
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


    private lateinit var mPageDao: PageDao

    private lateinit var mConnectionInformationDao: ConnectionInformationDao

    lateinit var reactionHistoryDao: ReactionHistoryDao

    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    lateinit var settingStore: SettingStore


    //private var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences

    override var urlPreviewStore: UrlPreviewStore? = null
        get() {
            if(field == null){
                field = JSoupUrlPreviewStore()
            }
            return field
        }
        private set
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
    private val mNoteCaptureAccountMap = HashMap<Account, NoteCapture>()
    private val mTimelineCaptureAccountMap = HashMap<Account, TimelineCapture>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    override lateinit var notificationSubscribeViewModel: NotificationSubscribeViewModel
    override lateinit var messageSubscriber: MessageSubscriber

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)
        settingStore = SettingStore(sharedPreferences)

        val database = Room.databaseBuilder(this, DataBase::class.java, "milk_database")
            .addMigrations(MIGRATION_1_2)
            .build()
        //connectionInstanceDao = database.connectionInstanceDao()
        mAccountDao = database.accountDao()

        mPageDao = database.pageDao()


        mConnectionInformationDao = database.connectionInformationDao()

        reactionHistoryDao = database.reactionHistoryDao()

        reactionUserSettingDao = database.reactionUserSettingDao()

        mEncryption = KeyStoreSystemEncryption(this)

        notificationSubscribeViewModel = NotificationSubscribeViewModel(this)
        messageSubscriber =
            MessageSubscriber(
                this
            )

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
                mPageDao.clearByAccount(account.id)
                mAccountDao.delete(account)
                synchronized(mStreamingAccountMap){
                    try{
                        val streaming = mStreamingAccountMap[account]
                        streaming?.disconnect()
                    }catch(e: Exception){
                        Log.e(TAG, "disconnect error", e)
                    }

                }
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "logout error", e)
            }
        }
    }





    override fun addPageInCurrentAccount(page: Page) {
        GlobalScope.launch(Dispatchers.IO){
            try{
                val aboutPageNumber = currentAccount.value?.pages?.size?: 0 + 1
                page.accountId = currentAccount.value?.account?.id
                if(page.pageNumber == null){
                    page.pageNumber = aboutPageNumber
                }
                mPageDao.insert(page)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun replaceAllPagesInCurrentAccount(pages: List<Page>){
        GlobalScope.launch(Dispatchers.IO){
            try{
                pages.forEach{
                    it.accountId = currentAccount.value?.account?.id
                }
                currentAccount.value?.let {
                    mPageDao.clearByAccount(it.account.id)
                    mPageDao.insertAll(pages)
                    loadAndInitializeAccounts()
                }
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removePageInCurrentAccount(page: Page){
        GlobalScope.launch(Dispatchers.IO){
            try{
                page.id?.let {
                    mPageDao.delete(it)
                    loadAndInitializeAccounts()
                }
            }catch(e: Exception){
                Log.e(TAG, "", e)
            }
        }
    }

    override fun removeAllPagesInCurrentAccount(pages: List<Page>){
        GlobalScope.launch(Dispatchers.IO){
            try{
                mPageDao.deleteAll(pages)
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

            if(current.pages.isEmpty()){
                saveDefaultPages(current, meta)
                return loadAndInitializeAccounts()
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

    private fun saveDefaultPages(account: AccountRelation, meta: Meta?){
        val isGlobalEnabled = !(meta?.disableGlobalTimeline?: false)
        val isLocalEnabled = !(meta?.disableLocalTimeline?: false)

        try{
            val defaultPages = ArrayList<Page>()
            defaultPages.add(PageableTemplate.homeTimeline(getString(R.string.home_timeline)))
            if(isLocalEnabled){
                defaultPages.add(PageableTemplate.hybridTimeline(getString(R.string.hybrid_timeline)))
            }
            if(isGlobalEnabled){
                defaultPages.add(PageableTemplate.globalTimeline(getString(R.string.global_timeline)))
            }
            defaultPages.forEachIndexed { index, page ->
                page.accountId = account.account.id
                page.pageNumber = index + 1
            }

            try{
                mPageDao.insertAll(defaultPages)
            }catch(e: Exception){
                Log.e(TAG, "default pages insert error", e)
            }

        }catch(e: Exception){
            Log.e(TAG, "default pages create error", e)
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
        Log.d(TAG, "getMainCapture")

        val isMainCaptureCreated: Boolean

        val mainCapture = synchronized(mMainCaptureAccountMap){
            val tmp = mMainCaptureAccountMap[account.account]
            isMainCaptureCreated = tmp == null
            (tmp?: MainCapture(GsonFactory.create())).apply{
                mMainCaptureAccountMap[account.account] = this
            }
        }

        if(isMainCaptureCreated){
            setupObserver(account, mainCapture)
        }

        return mainCapture
    }

    override fun setupObserver(account: AccountRelation, observer: Observer) {
        val ci = account.getCurrentConnectionInformation()
        synchronized(mStreamingAccountMap){
            var streaming = mStreamingAccountMap[account.account]
            if(streaming == null){
                streaming = StreamingAdapter(ci, getEncryption())
                mStreamingAccountMap[account.account] = streaming

            }

            synchronized(streaming.observerMap){
                if(streaming.observerMap[observer.id] == null){
                    streaming.putObserver(observer)
                }
            }
        }


    }

    override fun getNoteCapture(account: AccountRelation): NoteCapture {
        var noteCapture = synchronized(mNoteCaptureAccountMap){
            mNoteCaptureAccountMap[account.account]
        }

        if(noteCapture == null){
            noteCapture = NoteCapture(account.account)
            setupObserver(account, noteCapture)
            mNoteCaptureAccountMap[account.account] = noteCapture
        }
        return noteCapture


    }

    override fun getStreamingAdapter(account: AccountRelation): StreamingAdapter {
        val ci = account.getCurrentConnectionInformation()

        synchronized(mStreamingAccountMap){
            var streaming = mStreamingAccountMap[account.account]
            if(streaming == null){
                streaming = StreamingAdapter(ci, getEncryption())
            }
            mStreamingAccountMap[account.account] = streaming
            return streaming
        }
    }

    override fun getTimelineCapture(account: AccountRelation): TimelineCapture {
         var timelineCapture = synchronized(mTimelineCaptureAccountMap){
             mTimelineCaptureAccountMap[account.account]
         }

        if(timelineCapture == null){
            timelineCapture = TimelineCapture(account.account)
            setupObserver(account, timelineCapture)
            mTimelineCaptureAccountMap[account.account] = timelineCapture
        }
        return timelineCapture
    }


}