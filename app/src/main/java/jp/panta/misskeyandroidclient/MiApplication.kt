package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.Toast
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.logger.AndroidDefaultLogger
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.db.MediatorAccountRepository
import jp.panta.misskeyandroidclient.model.account.db.RoomAccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.KeyStoreSystemEncryption
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.instance.MediatorMetaStore
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import jp.panta.misskeyandroidclient.model.instance.db.InMemoryMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.MediatorMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.RoomMetaRepository
import jp.panta.misskeyandroidclient.model.instance.remote.RemoteMetaStore
import jp.panta.misskeyandroidclient.model.messaging.MessageRepository
import jp.panta.misskeyandroidclient.model.messaging.MessageStreamFilter
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import jp.panta.misskeyandroidclient.model.messaging.impl.InMemoryMessageDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageRepositoryImpl
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.impl.InMemoryNoteDataSource
import jp.panta.misskeyandroidclient.model.notes.impl.NoteRepositoryImpl
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notification.impl.InMemoryNotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.impl.NotificationRepositoryImpl
import jp.panta.misskeyandroidclient.model.settings.ColorSettingStore
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import jp.panta.misskeyandroidclient.model.url.*
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryAndMainChannelAdapter
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.model.users.impl.InMemoryUserDataSource
import jp.panta.misskeyandroidclient.model.users.impl.UserRepositoryImpl
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.impl.SocketWithAccountProviderImpl
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.util.platform.activeNetworkFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

//基本的な情報はここを返して扱われる
class MiApplication : Application(), MiCore {

    lateinit var reactionHistoryDao: ReactionHistoryDao

    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    lateinit var mSettingStore: SettingStore

    lateinit var draftNoteDao: DraftNoteDao

    lateinit var urlPreviewDAO: UrlPreviewDAO

    lateinit var mAccountRepository: AccountRepository

    lateinit var metaRepository: MetaRepository

    lateinit var metaStore: MetaStore

    private lateinit var sharedPreferences: SharedPreferences

    private val mAccountsState = MutableStateFlow(emptyList<Account>())
    private val mCurrentAccountState = MutableStateFlow<Account?>(null)


    var connectionStatus = MutableLiveData<ConnectionStatus>()

    private lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    private val mMisskeyAPIProvider: MisskeyAPIProvider = MisskeyAPIProvider()

    private lateinit var mNoteDataSource: NoteDataSource
    private lateinit var mUserDataSource: UserDataSource
    private lateinit var mNotificationDataSource: NotificationDataSource

    private lateinit var mNoteRepository: NoteRepository
    private lateinit var mUserRepository: UserRepository
    private lateinit var mNotificationRepository: NotificationRepository

    private lateinit var mUserRepositoryEventToFlow: UserRepositoryEventToFlow

    private lateinit var mSocketWithAccountProvider: SocketWithAccountProvider
    private lateinit var mSocketConnectionQueue: SocketConnectionQueue

    private lateinit var mNoteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider


    private lateinit var mChannelAPIWithAccountProvider: ChannelAPIWithAccountProvider

    private lateinit var mNoteCaptureAPIAdapter: NoteCaptureAPIAdapter

    private lateinit var mMessageDataSource: MessageDataSource
    private lateinit var mUnreadMessages: UnReadMessages
    private lateinit var mMessageRepository: MessageRepository

    private lateinit var mGetters: Getters


    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set


    @ExperimentalCoroutinesApi
    @FlowPreview
    override lateinit var messageStreamFilter: MessageStreamFilter


    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override var loggerFactory: Logger.Factory = AndroidDefaultLogger.Factory
    private val logger = loggerFactory.create("MiApplication")

    private lateinit var mActiveNetworkState: Flow<Boolean>
    private var mIsActiveNetwork: Boolean = false

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        val config = BundledEmojiCompatConfig(this)
            .setReplaceAll(true)
        if(BuildConfig.DEBUG){
            config.setEmojiSpanIndicatorColor(Color.GREEN)
                .setEmojiSpanIndicatorEnabled(true)
        }
        EmojiCompat.init(config)

        mActiveNetworkState = activeNetworkFlow().shareIn(applicationScope, SharingStarted.Eagerly)

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
        val roomAccountRepository = RoomAccountRepository(database, sharedPreferences, database.accountDAO(), database.pageDAO())
        mAccountRepository = MediatorAccountRepository(roomAccountRepository)


        reactionHistoryDao = database.reactionHistoryDao()

        reactionUserSettingDao = database.reactionUserSettingDao()

        draftNoteDao = database.draftNoteDao()

        mEncryption = KeyStoreSystemEncryption(this)

        urlPreviewDAO = database.urlPreviewDAO()

        metaRepository = MediatorMetaRepository(RoomMetaRepository(database.metaDAO()), InMemoryMetaRepository())

        metaStore = MediatorMetaStore(metaRepository, RemoteMetaStore(), true)

        mNoteDataSource = InMemoryNoteDataSource(loggerFactory)
        mNoteRepository = NoteRepositoryImpl(this)

        mUserDataSource = InMemoryUserDataSource(loggerFactory)
        mUserRepository = UserRepositoryImpl(this)

        mNotificationDataSource = InMemoryNotificationDataSource()

        mUserRepositoryEventToFlow = UserRepositoryEventToFlow(mUserDataSource)

        mSocketWithAccountProvider = SocketWithAccountProviderImpl(
            getEncryption(),
            mAccountRepository,
            loggerFactory,
            { account, socket ->
                mSocketConnectionQueue.connect(account)
                connectChannel(account, ChannelAPI.Type.MAIN).onEach {
                    // 各種DataSourceなどの各種変更イベントを通達する
                    runCatching {
                        if(it is ChannelBody.Main.Notification) {
                            getGetters().notificationRelationGetter.get(account, it.body)
                        }
                    }

                }.launchIn(applicationScope + Dispatchers.IO)
                socket.addStateEventListener { e ->
                    applicationScope.launch {
                        handleSocketStateEvent(account, e)
                    }
                }
            },
            { _, _ ->
                mIsActiveNetwork
            }
        )
        mSocketConnectionQueue = SocketConnectionQueue(mSocketWithAccountProvider, applicationScope, Dispatchers.IO, loggerFactory)

        mNoteCaptureAPIWithAccountProvider = NoteCaptureAPIWithAccountProvider(mSocketWithAccountProvider, loggerFactory)

        mNoteCaptureAPIAdapter = NoteCaptureAPIAdapter(
            mAccountRepository,
            mNoteDataSource,
            mNoteCaptureAPIWithAccountProvider,
            loggerFactory,
            applicationScope,
            Dispatchers.IO
        )


        mChannelAPIWithAccountProvider = ChannelAPIWithAccountProvider(mSocketWithAccountProvider, loggerFactory)

        InMemoryMessageDataSource(mAccountRepository).also {
            mMessageDataSource = it
            mUnreadMessages = it
        }
        mMessageRepository = MessageRepositoryImpl(this)

        mGetters = Getters(mNoteDataSource, mUserDataSource, mNotificationDataSource, mMessageDataSource)

        messageStreamFilter = MessageStreamFilter(this)

        mNotificationRepository = NotificationRepositoryImpl(
            mNotificationDataSource,
            applicationScope,
            mSocketWithAccountProvider,
            mAccountRepository,
            getGetters().notificationRelationGetter,
            Dispatchers.IO
        )

        val userRepositoryAndMainChanelAPIAdapter = UserRepositoryAndMainChannelAdapter(mUserDataSource, mChannelAPIWithAccountProvider)
        // NOTE: 何度もchannelの接続と切断が繰り返される可能性があるがAccountに対してそこまでアクションをとる可能性は低い
        mAccountsState.flatMapLatest { list ->
            list.map{ ac ->
                userRepositoryAndMainChanelAPIAdapter.listen(ac)
            }.merge()
        }.launchIn(applicationScope + Dispatchers.IO)

        mAccountRepository.addEventListener { ev ->
            applicationScope.launch(Dispatchers.IO) {
                try{
                    if(ev is AccountRepository.Event.Deleted) {
                        mSocketWithAccountProvider.get(ev.accountId)?.disconnect()
                    }
                    loadAndInitializeAccounts()
                }catch(e: Exception) {
                    logger.error("アカウントの更新があったのでStateを更新しようとしたところ失敗しました。", e)
                }
            }
        }

        applicationScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                AccountMigration(database.accountDao(), mAccountRepository, sharedPreferences).executeMigrate()
                loadAndInitializeAccounts()
            }catch(e: Exception){
                logger.error("load account error", e = e)
                //isSuccessCurrentAccount.postValue(false)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesChangedListener)

        mActiveNetworkState.distinctUntilChanged().onEach {
            logger.debug("接続状態が変化:${if(it) "接続" else "未接続"}")
            mIsActiveNetwork = it
            if(it) {
                // NOTE: ネットワークの接続状態が非アクティブからアクティブに変化したのでSocketの再接続処理を行う
                mSocketWithAccountProvider.all().filterNot { socket ->
                    socket.state() == Socket.State.Connected
                }.forEach { socket ->
                    mSocketConnectionQueue.connect(socket)
                }
            }
        }.launchIn(applicationScope + Dispatchers.IO)


    }

    override fun getAccounts(): StateFlow<List<Account>> {
        return mAccountsState
    }

    override fun getCurrentAccount(): StateFlow<Account?> {
        return mCurrentAccountState
    }

    override suspend fun getAccount(accountId: Long): Account {
        return mAccountRepository.get(accountId)
    }

    override fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }



    override fun getNoteCaptureAdapter(): NoteCaptureAPIAdapter {
        return mNoteCaptureAPIAdapter
    }

    override fun getAccountRepository(): AccountRepository {
        return mAccountRepository
    }

    override fun getNotificationDataSource(): NotificationDataSource {
        return mNotificationDataSource
    }

    override fun getNotificationRepository(): NotificationRepository {
        return mNotificationRepository
    }

    override fun getMessageDataSource(): MessageDataSource {
        return mMessageDataSource
    }

    override fun getMessageRepository(): MessageRepository {
        return mMessageRepository
    }

    override fun getUnreadMessages(): UnReadMessages {
        return mUnreadMessages
    }

    override fun getGetters(): Getters {
        return mGetters
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
                    mCurrentAccountState.value
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[url] = store
            store
        }
    }

    override fun setCurrentAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                mAccountRepository.setCurrentAccount(account)
                loadAndInitializeAccounts()
            }catch(e: Exception){
                logger.error("switchAccount error", e)
            }
        }
    }


    override fun logoutAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                mAccountRepository.delete(account)
            }catch(e: Exception){

            }

            try{
                loadAndInitializeAccounts()
            }catch(e: Exception){

            }

        }
    }



    override fun addAccount(account: Account) {
        applicationScope.launch(Dispatchers.IO){
            try{
                mAccountRepository.add(account, true)

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
                mAccountRepository.add(account.copy(pages = pages), true)
            }catch(e: Exception){
                logger.error("アカウント更新処理中にエラー発生", e)
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
                mAccountRepository.add(account.copy(pages = removed), true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){
                logger.error("ページを削除しようとしたところエラーが発生した", e)
            }
        }
    }



    override fun removePageInCurrentAccount(page: Page) {
        applicationScope.launch(Dispatchers.IO){
            try{
                val current = mAccountRepository.getCurrentAccount()
                val removed = current.copy(pages = current.pages.filterNot{
                    it == page || it.pageId == page.pageId
                })
                mAccountRepository.add(removed, true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            }
        }
    }

    override fun replaceAllPagesInCurrentAccount(pages: List<Page>) {
        applicationScope.launch(Dispatchers.IO){
            try{
                val updated = mAccountRepository.getCurrentAccount().copy(pages = pages)
                mAccountRepository.add(updated, true)
                loadAndInitializeAccounts()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            }
        }
    }

    private suspend fun getCurrentAccountErrorSafe(): Account?{
        return try{
            mAccountRepository.getCurrentAccount()
        }catch(e: AccountNotFoundException){
            logger.error("アカウントローディング中に失敗しました", e)
            connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
            return null
        }
    }


    override fun getDraftNoteDAO(): DraftNoteDao {
        return draftNoteDao
    }

    override fun createFileUploader(account: Account): FileUploader {
        return OkHttpDriveFileUploader(
            context = this,
            account = account,
            gson = GsonFactory.create(),
            encryption = getEncryption()
        )
    }

    override fun getSettingStore(): SettingStore {
        return this.mSettingStore
    }

    override fun getNoteDataSource(): NoteDataSource {
        return mNoteDataSource
    }

    override fun getNoteRepository(): NoteRepository {
        return mNoteRepository
    }

    override fun getUserDataSource(): UserDataSource {
        return mUserDataSource
    }

    override fun getUserRepository(): UserRepository {
        return mUserRepository
    }

    override fun getUserRepositoryEventToFlow(): UserRepositoryEventToFlow {
        return mUserRepositoryEventToFlow
    }

    override suspend fun getChannelAPI(account: Account): ChannelAPI {
        return mChannelAPIWithAccountProvider.get(account)
    }


    private suspend fun loadAndInitializeAccounts(){
        try{
            val current: Account
            val tmpAccounts = try{
                current = mAccountRepository.getCurrentAccount()

                mAccountRepository.findAll()
            }catch(e: AccountNotFoundException){
                connectionStatus.postValue(ConnectionStatus.ACCOUNT_ERROR)
                return
            }


            logger.debug(this.javaClass.simpleName, "load account result : $current")


            val meta = loadInstanceMetaAndSetupAPI(current.instanceDomain)

            if(meta == null){
                connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            }

            logger.debug("accountId:${current.accountId}, account:$current")
            if(current.pages.isEmpty()){
                saveDefaultPages(current, meta)
                return loadAndInitializeAccounts()
            }

            mCurrentAccountState.value = current
            mAccountsState.value = tmpAccounts
            connectionStatus.postValue(ConnectionStatus.SUCCESS)

            setUpMetaMap(tmpAccounts)

        }catch(e: Exception){
            //isSuccessCurrentAccount.postValue(false)
            logger.error( "初期読み込みに失敗しまちた", e)
        }
    }

    private suspend fun saveDefaultPages(account: Account, meta: Meta?){
        try{
            val pages = makeDefaultPages(account, meta)
            mAccountRepository.add(account.copy(pages = pages), true)
        }catch(e: Exception){
            logger.error("default pages create error", e)
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

    /**
     * Socketの通信状態をhandleする。
     */
    private suspend fun handleSocketStateEvent(account: Account,  state: Socket.State) {
        if(state is Socket.State.Failure
            && mIsActiveNetwork
            && !(mNoteCaptureAPIWithAccountProvider.get(account).isEmpty()
                    && mChannelAPIWithAccountProvider.get(account).isEmpty())
        ) {
            logger.debug("ネットワークアクティブ、WebSocket未接続なので再接続を試みる")
            mSocketConnectionQueue.connect(account)
        }
    }


    override fun getCurrentInstanceMeta(): Meta?{
        return synchronized(mMetaInstanceUrlMap){
            mCurrentAccountState.value?.instanceDomain?.let{ url ->
                mMetaInstanceUrlMap[url]
            }
        }
    }

    private suspend fun setUpMetaMap(accounts: List<Account>){
        try{
            // NOTE: メモリ等にインスタンスを読み込んでおく
            accounts.forEach { ac ->
                loadInstanceMetaAndSetupAPI(ac.instanceDomain)
            }
        }catch(e: Exception){
            logger.error("meta取得中にエラー発生", e = e)
        }
    }


    private suspend fun loadInstanceMetaAndSetupAPI(instanceDomain: String): Meta?{
        try{
            val meta = metaStore.get(instanceDomain)


            logger.debug("load meta result ${meta?.let{"成功"}?: "失敗"} ")

            meta?: return null

            synchronized(mMetaInstanceUrlMap){
                mMetaInstanceUrlMap[instanceDomain] = meta
            }

            return meta

        }catch(e: Exception){
            logger.error("metaの読み込み一連処理に失敗したでち", e)
            connectionStatus.postValue(ConnectionStatus.NETWORK_ERROR)
            return null
        }


    }

    override fun getMisskeyAPI(account: Account): MisskeyAPI{
        return getMisskeyAPI(account.instanceDomain)
    }

    override fun getMisskeyAPI(instanceDomain: String): MisskeyAPI {
        return mMisskeyAPIProvider.get(instanceDomain, mMetaInstanceUrlMap[instanceDomain]?.getVersion())
    }

    override fun getMisskeyAPIProvider(): MisskeyAPIProvider {
        return mMisskeyAPIProvider
    }

    override fun getEncryption(): Encryption {
        return mEncryption
    }

    override fun getNoteCaptureAPI(account: Account): NoteCaptureAPI {
        return mNoteCaptureAPIWithAccountProvider.get(account)
    }



    private val sharedPreferencesChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when(key){
                UrlPreviewSourceSetting.URL_PREVIEW_SOURCE_TYPE_KEY -> {
                    mAccountsState.value.forEach {
                        getUrlPreviewStore(it, true)
                    }
                }
            }
        }

    private fun<T> List<T>.toArrayList(): ArrayList<T>{
        return ArrayList(this)
    }

    /**
     * プラットフォームへの依存をなるべく少なくしたかったためApplicationから作成している
     */
    override fun createNote(createNote: CreateNote) {
        applicationScope.launch(Dispatchers.IO) {
            runCatching {
                getNoteRepository().create(createNote)
            }.onSuccess {
                logger.debug("投稿に成功しました。")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MiApplication, getString(R.string.success), Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                logger.error("投稿に失敗しました。", e = it)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MiApplication, "error: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun MiCore.connectChannel(account: Account, channelType: ChannelAPI.Type): Flow<ChannelBody> {
        return suspend {
            getChannelAPI(account)
        }.asFlow().flatMapLatest {
            it.connect(channelType)
        }
    }

}