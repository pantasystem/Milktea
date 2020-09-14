package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.db.RoomAccountRepository
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
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
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import jp.panta.misskeyandroidclient.model.url.*
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationSubscribeViewModel
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap
import jp.panta.misskeyandroidclient.model.account.page.Page

//基本的な情報はここを返して扱われる
class MiApplication : Application(), MiCore {
    companion object{
        const val CURRENT_USER_ID = "jp.panta.misskeyandroidclient.MiApplication.CurrentUserId"
        const val TAG = "MiApplication"
    }

    /*var connectionInstanceDao: ConnectionInstanceDao? = null
        private set*/




    lateinit var reactionHistoryDao: ReactionHistoryDao

    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    lateinit var mSettingStore: SettingStore

    lateinit var draftNoteDao: DraftNoteDao

    lateinit var urlPreviewDAO: UrlPreviewDAO

    lateinit var accountRepository: AccountRepository


    //private var nowInstanceMeta: Meta? = null

    private lateinit var sharedPreferences: SharedPreferences


    /*var misskeyAPIService: MisskeyAPI? = null
        private set*/

    //private var misskeyAPIServiceDomainMap: Map<String, MisskeyAPI>? = null

    // private var mConnectionInstance: ConnectionInstance? = null

    private val mAccounts = MutableLiveData<List<Account>>()
    private val mCurrentAccount = MutableLiveData<Account>()


    //var isSuccessCurrentAccount = MutableLiveData<Boolean>()
    var connectionStatus = MutableLiveData<ConnectionStatus>()

    private lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    private val mMisskeyAPIUrlMap = HashMap<String, Pair<Version?, MisskeyAPI>>()

    private val mStreamingAccountMap = HashMap<Long, StreamingAdapter>()
    private val mMainCaptureAccountMap = HashMap<Long, MainCapture>()
    private val mNoteCaptureAccountMap = HashMap<Long, NoteCapture>()
    private val mTimelineCaptureAccountMap = HashMap<Long, TimelineCapture>()

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    override lateinit var notificationSubscribeViewModel: NotificationSubscribeViewModel
    override lateinit var messageSubscriber: MessageSubscriber

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)
        mSettingStore = SettingStore(sharedPreferences)

        val database = Room.databaseBuilder(this, DataBase::class.java, "milk_database")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .build()
        //connectionInstanceDao = database.connectionInstanceDao()
        accountRepository = RoomAccountRepository(database.accountDAO(), database.pageDAO(), sharedPreferences)



        reactionHistoryDao = database.reactionHistoryDao()

        reactionUserSettingDao = database.reactionUserSettingDao()

        draftNoteDao = database.draftNoteDao()

        mEncryption = KeyStoreSystemEncryption(this)

        urlPreviewDAO = database.urlPreviewDAO()

        notificationSubscribeViewModel = NotificationSubscribeViewModel(this)
        messageSubscriber =
            MessageSubscriber(
                this
            )

        applicationScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "load account error", e)
                //isSuccessCurrentAccount.postValue(false)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesChangedListener)
    }

    override fun getAccounts(): LiveData<List<Account>> {
        return mAccounts
    }

    override fun getCurrentAccount(): LiveData<Account> {
        return mCurrentAccount
    }

    override suspend fun getAccount(accountId: Long): Account {
        return accountRepository.get(accountId)
    }

    override fun getUrlPreviewStore(account: Account): UrlPreviewStore? {
        return getUrlPreviewStore(account, false)
    }


    private fun getUrlPreviewStore(account: Account, isReplace: Boolean): UrlPreviewStore?{
        return account.instanceDomain.let{ accountUrl ->
            val url = mSettingStore.urlPreviewSetting.getSummalyUrl()?: accountUrl

            var store = mUrlPreviewStoreInstanceBaseUrlMap[url]
            if(store == null || isReplace){
                store = UrlPreviewStoreFactory(
                    urlPreviewDAO
                    ,mSettingStore.urlPreviewSetting.getSourceType(),
                    mSettingStore.urlPreviewSetting.getSummalyUrl(),
                    mCurrentAccount.value
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[url] = store
            store
        }
    }

    override fun setCurrentAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                mCurrentAccount.postValue(accountRepository.setCurrentAccount(account))
            }catch(e: Exception){
                Log.e(TAG, "switchAccount error", e)
            }
        }
    }

    /*override fun switchAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{

                setCurrentUserId(account.id)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.d(TAG, "add or change account error", e)
            }
        }
    }*/

    override fun logoutAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                accountRepository.delete(account)
            }catch(e: Exception){

            }
            try{
                closeAccountResource(account)
            }catch(e: Exception){
                Log.e(TAG, "disconnect error", e)
            }

            try{
                loadAndInitializeAccounts()
            }catch(e: Exception){

            }

        }
    }


    private fun closeAccountResource(account: Account){
        synchronized(mStreamingAccountMap){
            mStreamingAccountMap.remove(account.accountId)?.disconnect()
        }

        synchronized(mMainCaptureAccountMap){
            mStreamingAccountMap.remove(account.accountId)
        }

        synchronized(mTimelineCaptureAccountMap){
            mStreamingAccountMap.remove(account.accountId)
        }

        synchronized(mNoteCaptureAccountMap){
            mNoteCaptureAccountMap.remove(account.accountId)
        }
    }

    override fun addAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                accountRepository.add(account, true)

                loadAndInitializeAccounts()
            }catch(e: Exception){

            }
        }
    }


    override fun addPageInCurrentAccount(page: Page) {
        applicationScope.launch(Dispatchers.IO){
            val account = getCurrentAccountErrorSafe()
                ?: return@launch
            val pages = account.pages.toArrayList()
            pages.add(page)
            try{
                val updated= accountRepository.add(account.copy(pages = pages), true)
                mCurrentAccount.postValue(updated)
            }catch(e: Exception){
                Log.e(TAG, "アカウント更新処理中にエラー発生", e)
            }
            loadAndInitializeAccounts()
        }
    }

    override fun removeAllPagesInCurrentAccount(pages: List<Page>) {
        applicationScope.launch(Dispatchers.IO) {

            val account = getCurrentAccountErrorSafe()?: return@launch
            val removed = account.pages.filterNot{ i ->
                i.pageId == pages.firstOrNull { j ->
                    i.pageId == i.pageId
                }?.pageId
            }
            try{
                accountRepository.add(account.copy(pages = removed), true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){

            }
        }
    }



    override fun removePageInCurrentAccount(page: Page) {
        applicationScope.launch(Dispatchers.IO){
            try{
                val current = accountRepository.getCurrentAccount()
                val removed = current.copy(pages = current.pages.filterNot{
                    it == page || it.pageId == page.pageId
                })
                accountRepository.add(removed, true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            }
        }
    }

    override fun replaceAllPagesInCurrentAccount(pages: List<Page>) {
        applicationScope.launch(Dispatchers.IO){
            try{
                val updated = accountRepository.getCurrentAccount().copy(pages = pages)
                accountRepository.add(updated, true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            }
        }
    }

    private suspend fun getCurrentAccountErrorSafe(): Account?{
        return try{
            accountRepository.getCurrentAccount()
        }catch(e: AccountNotFoundException){
            Log.e(TAG, "アカウントローディング中に失敗しました", e)
            connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            return null
        }
    }





    override fun setupObserver(account: Account, observer: Observer) {
        synchronized(mStreamingAccountMap) {
            var streaming = mStreamingAccountMap[account.accountId]
            if (streaming == null) {
                streaming = StreamingAdapter(account, getEncryption())
                mStreamingAccountMap[account.accountId] = streaming

            }

            synchronized(streaming.observerMap) {
                if (streaming.observerMap[observer.id] == null) {
                    streaming.putObserver(observer)
                }
            }
        }
    }




    override fun getSettingStore(): SettingStore {
        return this.mSettingStore
    }


    private suspend fun loadAndInitializeAccounts(){
        try{
            val current: Account
            val tmpAccounts = try{
                current = accountRepository.getCurrentAccount()

                accountRepository.findAll()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
                return
            }


            Log.d(this.javaClass.simpleName, "load account result : $current")


            val meta = loadInstanceMetaAndSetupAPI(current.instanceDomain)

            if(meta == null){
                connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            }

            Log.d(TAG, "accountId:${current.accountId}, account:$current")
            if(current.pages.isEmpty()){
                saveDefaultPages(current, meta)
                return loadAndInitializeAccounts()
            }

            mCurrentAccount.postValue(current)
            mAccounts.postValue(tmpAccounts)
            connectionStatus.postValue(ConnectionStatus.SUCCESS)

            setUpMetaMap(tmpAccounts)

        }catch(e: Exception){
            //isSuccessCurrentAccount.postValue(false)
            Log.e(TAG, "初期読み込みに失敗しまちた", e)
        }
    }

    private suspend fun saveDefaultPages(account: Account, meta: Meta?){
        try{
            val pages = makeDefaultPages(account, meta)
            accountRepository.add(account.copy(pages = pages), true)
        }catch(e: Exception){
            Log.e(TAG, "default pages create error", e)
        }
    }

    private fun makeDefaultPages(account: Account, meta: Meta?): List<Page>{
        val isGlobalEnabled = !(meta?.disableGlobalTimeline?: false)
        val isLocalEnabled = !(meta?.disableLocalTimeline?: false)

        val defaultPages = ArrayList<Page>()
        defaultPages.add(PageableTemplate(account).homeTimeline(getString(R.string.home_timeline)))
        if(isLocalEnabled){
            defaultPages.add(PageableTemplate(account).hybridTimeline(getString(R.string.hybrid_timeline)))
        }
        if(isGlobalEnabled){
            defaultPages.add(PageableTemplate(account).globalTimeline(getString(R.string.global_timeline)))
        }
        return defaultPages.mapIndexed { index, page ->
            page.also {
                page.weight = index
            }
        }
    }


    override fun getCurrentInstanceMeta(): Meta?{
        return synchronized(mMetaInstanceUrlMap){
            mCurrentAccount.value?.instanceDomain?.let{ url ->
                mMetaInstanceUrlMap[url]
            }
        }
    }

    private fun setUpMetaMap(accounts: List<Account>){
        try{
            accounts.forEach { ac ->
                loadInstanceMetaAndSetupAPI(ac.instanceDomain)
            }
        }catch(e: Exception){
            Log.e(TAG, "meta取得中にエラー発生", e)
        }
    }


    private fun loadInstanceMetaAndSetupAPI(instanceDomain: String): Meta?{
        try{
            val meta = synchronized(mMisskeyAPIUrlMap){
                try{
                    mMetaInstanceUrlMap[instanceDomain]
                }catch(e: Exception){
                    Log.d(TAG, "metaマップからの取得に失敗したでち")
                    null
                }
            } ?: try{
                MisskeyGetMeta.getMeta(instanceDomain).execute().body()
            }catch(e: Exception){
                Log.d(TAG, "metaをオンラインから取得するのに失敗したでち")
                connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)

                null
            }


            Log.d(TAG, "load meta result ${meta?.let{"成功"}?: "失敗"} ")

            meta?: return null

            synchronized(mMetaInstanceUrlMap){
                mMetaInstanceUrlMap[instanceDomain] = meta
            }
            synchronized(mMisskeyAPIUrlMap){
                val versionAndApi = mMisskeyAPIUrlMap[instanceDomain]
                if(versionAndApi?.first != meta.getVersion()){
                    val newApi = MisskeyAPIServiceBuilder.build(instanceDomain, meta.getVersion())
                    mMisskeyAPIUrlMap[instanceDomain] = Pair(meta.getVersion(), newApi)
                }
            }
            return meta

        }catch(e: Exception){
            Log.e(TAG, "metaの読み込み一連処理に失敗したでち", e)
            connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            return null
        }


    }

    override fun getMisskeyAPI(account: Account): MisskeyAPI{
        return getMisskeyAPI(account.instanceDomain)
    }

    override fun getMisskeyAPI(instanceDomain: String): MisskeyAPI {
        synchronized(mMisskeyAPIUrlMap){
            val api = mMisskeyAPIUrlMap[instanceDomain]
                ?: Pair(null, MisskeyAPIServiceBuilder.build(instanceDomain))
            mMisskeyAPIUrlMap[instanceDomain] = api
            return api.second
        }
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

    override fun getMainCapture(account: Account): MainCapture{
        Log.d(TAG, "getMainCapture")

        val isMainCaptureCreated: Boolean

        val mainCapture = synchronized(mMainCaptureAccountMap){
            val tmp = mMainCaptureAccountMap[account.accountId]
            isMainCaptureCreated = tmp == null
            (tmp?: MainCapture(account, GsonFactory.create())).apply{
                mMainCaptureAccountMap[account.accountId] = this
            }
        }

        if(isMainCaptureCreated){
            setupObserver(account, mainCapture)
        }
        validateObserverAccount(mainCapture, account)


        return mainCapture
    }



    override fun getNoteCapture(account: Account): NoteCapture {
        var noteCapture = synchronized(mNoteCaptureAccountMap){
            mNoteCaptureAccountMap[account.accountId]
        }

        if(noteCapture == null){
            noteCapture = NoteCapture(account)
            setupObserver(account, noteCapture)
            mNoteCaptureAccountMap[account.accountId] = noteCapture
        }
        validateObserverAccount(noteCapture, account)

        return noteCapture


    }


    private fun getStreamingAdapter(account: Account): StreamingAdapter {

        synchronized(mStreamingAccountMap){
            var streaming = mStreamingAccountMap[account.accountId]
            if(streaming == null){
                streaming = StreamingAdapter(account, getEncryption())
            }
            mStreamingAccountMap[account.accountId] = streaming
            return streaming
        }
    }

    override fun getTimelineCapture(account: Account): TimelineCapture {
         var timelineCapture = synchronized(mTimelineCaptureAccountMap){
             mTimelineCaptureAccountMap[account.accountId]
         }

        if(timelineCapture == null){
            timelineCapture = TimelineCapture(account, getSettingStore())
            setupObserver(account, timelineCapture)
            mTimelineCaptureAccountMap[account.accountId] = timelineCapture
        }
        validateObserverAccount(timelineCapture, account)
        return timelineCapture
    }

    private fun validateObserverAccount(observer: Observer, account: Account){
        if(observer.account == account){
            Log.d("MiApplication", "Observerのアカウント一致正常です！！")
        }else{
            Log.e("MiApplication" ,"Observerのアカウントが一致しません！！エラー")
        }
    }

    private val sharedPreferencesChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when(key){
                UrlPreviewSourceSetting.URL_PREVIEW_SOURCE_TYPE_KEY -> {
                    mAccounts.value?.forEach {
                        getUrlPreviewStore(it, true)
                    }
                }
            }
        }

    private fun<T> List<T>.toArrayList(): ArrayList<T>{
        return ArrayList(this)
    }

}