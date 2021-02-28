package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.logger.AndroidDefaultLogger
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.db.RoomAccountRepository
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.settings.ColorSettingStore
import jp.panta.misskeyandroidclient.model.settings.SettingStore
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
import jp.panta.misskeyandroidclient.model.instance.MediatorMetaStore
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import jp.panta.misskeyandroidclient.model.instance.db.RoomMetaRepository
import jp.panta.misskeyandroidclient.model.instance.remote.RemoteMetaStore
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.impl.InMemoryNoteRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.model.users.impl.InMemoryUserRepository
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.impl.SocketWithAccountProviderImpl

//基本的な情報はここを返して扱われる
class MiApplication : Application(), MiCore {
    companion object{
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

    lateinit var metaRepository: MetaRepository

    lateinit var metaStore: MetaStore



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

    private lateinit var mNoteRepository: NoteRepository
    private lateinit var mUserRepository: UserRepository
    private lateinit var mUserRepositoryEventToFlow: UserRepositoryEventToFlow

    private lateinit var mSocketWithAccountProvider: SocketWithAccountProvider

    private lateinit var mNoteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider

    private lateinit var mNoteCaptureAPIAdapter: NoteCaptureAPIAdapter

    private lateinit var mChannelAPIWithAccountProvider: ChannelAPIWithAccountProvider

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    override lateinit var notificationSubscribeViewModel: NotificationSubscribeViewModel
    override lateinit var messageSubscriber: MessageSubscriber

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override var loggerFactory: Logger.Factory = AndroidDefaultLogger.Factory


    override fun onCreate() {
        super.onCreate()

        val config = BundledEmojiCompatConfig(this)
            .setReplaceAll(true)
        if(BuildConfig.DEBUG){
            config.setEmojiSpanIndicatorColor(Color.GREEN)
                .setEmojiSpanIndicatorEnabled(true)
        }
        EmojiCompat.init(config)

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)
        mSettingStore = SettingStore(sharedPreferences)

        val database = Room.databaseBuilder(this, DataBase::class.java, "milk_database")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .build()
        //connectionInstanceDao = database.connectionInstanceDao()
        accountRepository = RoomAccountRepository(database, sharedPreferences, database.accountDAO(), database.pageDAO())



        reactionHistoryDao = database.reactionHistoryDao()

        reactionUserSettingDao = database.reactionUserSettingDao()

        draftNoteDao = database.draftNoteDao()

        mEncryption = KeyStoreSystemEncryption(this)

        urlPreviewDAO = database.urlPreviewDAO()

        metaRepository = RoomMetaRepository(database.metaDAO())

        metaStore = MediatorMetaStore(metaRepository, RemoteMetaStore(), true)

        mNoteRepository = InMemoryNoteRepository(loggerFactory)
        mUserRepository = InMemoryUserRepository()
        mUserRepositoryEventToFlow = UserRepositoryEventToFlow()

        mSocketWithAccountProvider = SocketWithAccountProviderImpl(
            getEncryption(),
            loggerFactory,
            { _, socket ->
               socket.connect()
            }
        )

        mNoteCaptureAPIWithAccountProvider = NoteCaptureAPIWithAccountProvider(mSocketWithAccountProvider, loggerFactory)

        mNoteCaptureAPIAdapter = NoteCaptureAPIAdapter(
            accountRepository,
            mNoteRepository,
            mNoteCaptureAPIWithAccountProvider,
            loggerFactory,
            applicationScope,
            Dispatchers.IO
        )

        mChannelAPIWithAccountProvider = ChannelAPIWithAccountProvider(mSocketWithAccountProvider)

        notificationSubscribeViewModel = NotificationSubscribeViewModel(this)
        messageSubscriber =
            MessageSubscriber(
                this
            )


        applicationScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                AccountMigration(database.accountDao(), accountRepository, sharedPreferences).executeMigrate()
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

    override fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }


    override fun getNoteCaptureAPIAdapter(): NoteCaptureAPIAdapter {
        return mNoteCaptureAPIAdapter
    }



    private fun getUrlPreviewStore(account: Account, isReplace: Boolean): UrlPreviewStore{
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
                accountRepository.setCurrentAccount(account)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                Log.e(TAG, "switchAccount error", e)
            }
        }
    }


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
        mSocketWithAccountProvider.get(account).disconnect()
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
                    i.pageId == j.pageId
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








    override fun getSettingStore(): SettingStore {
        return this.mSettingStore
    }

    override fun getNoteRepository(): NoteRepository {
        return mNoteRepository
    }


    override fun getUserRepository(): UserRepository {
        return mUserRepository
    }

    override fun getUserRepositoryEventToFlow(): UserRepositoryEventToFlow {
        return mUserRepositoryEventToFlow
    }

    override fun getChannelAPI(account: Account): ChannelAPI {
        return mChannelAPIWithAccountProvider.get(account)
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

    private suspend fun setUpMetaMap(accounts: List<Account>){
        try{
            accounts.forEach { ac ->
                loadInstanceMetaAndSetupAPI(ac.instanceDomain)
            }
        }catch(e: Exception){
            Log.e(TAG, "meta取得中にエラー発生", e)
        }
    }


    private suspend fun loadInstanceMetaAndSetupAPI(instanceDomain: String): Meta?{
        try{
            val meta = synchronized(mMisskeyAPIUrlMap){
                try{
                    mMetaInstanceUrlMap[instanceDomain]
                }catch(e: Exception){
                    Log.d(TAG, "metaマップからの取得に失敗したでち")
                    null
                }
            } ?: try{
                metaStore.get(instanceDomain)
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