package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.model.gallery.impl.createGalleryRepository
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.group.GroupRepository
import jp.panta.misskeyandroidclient.model.group.impl.GroupRepositoryImpl
import jp.panta.misskeyandroidclient.model.instance.MediatorMetaStore
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.instance.MetaStore
import jp.panta.misskeyandroidclient.model.instance.db.InMemoryMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.MediatorMetaRepository
import jp.panta.misskeyandroidclient.model.instance.db.RoomMetaRepository
import jp.panta.misskeyandroidclient.model.instance.remote.RemoteMetaStore
import jp.panta.misskeyandroidclient.model.messaging.MessageObserver
import jp.panta.misskeyandroidclient.model.messaging.MessageRepository
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageRepositoryImpl
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.impl.ReactionHistoryPaginatorImpl
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
import jp.panta.misskeyandroidclient.model.notification.impl.NotificationRepositoryImpl
import jp.panta.misskeyandroidclient.model.settings.ColorSettingStore
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.settings.UrlPreviewSourceSetting
import jp.panta.misskeyandroidclient.model.streaming.MediatorMainEventDispatcher
import jp.panta.misskeyandroidclient.model.sw.register.SubscriptionRegistration
import jp.panta.misskeyandroidclient.model.sw.register.SubscriptionUnRegistration
import jp.panta.misskeyandroidclient.model.url.*
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserRepositoryEventToFlow
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.util.platform.activeNetworkFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//基本的な情報はここを返して扱われる
@HiltAndroidApp
class MiApplication : Application(), MiCore {

    @Inject lateinit var database: DataBase
    @Inject lateinit var reactionHistoryDao: ReactionHistoryDao

    @Inject lateinit var reactionUserSettingDao: ReactionUserSettingDao

    @Inject lateinit var mSettingStore: SettingStore

    lateinit var draftNoteDao: DraftNoteDao

    lateinit var urlPreviewDAO: UrlPreviewDAO

    @Inject lateinit var mAccountRepository: AccountRepository

    private lateinit var metaRepository: MetaRepository

    private lateinit var metaStore: MetaStore

    private lateinit var sharedPreferences: SharedPreferences

    private val mAccountsState = MutableStateFlow(emptyList<Account>())
    private val mCurrentAccountState = MutableStateFlow<Account?>(null)


    var connectionStatus = MutableLiveData<ConnectionStatus>()

    @Inject lateinit var mEncryption: Encryption

    private val mMetaInstanceUrlMap = HashMap<String, Meta>()
    @Inject lateinit var mMisskeyAPIProvider: MisskeyAPIProvider

    @Inject lateinit var mNoteDataSource: NoteDataSource
    @Inject lateinit var mUserDataSource: UserDataSource
    @Inject lateinit var mNotificationDataSource: NotificationDataSource
    @Inject lateinit var mMessageDataSource: MessageDataSource
    @Inject lateinit var mReactionHistoryDataSource: ReactionHistoryDataSource
    @Inject lateinit var mGroupDataSource: GroupDataSource
    @Inject lateinit var mFilePropertyDataSource: FilePropertyDataSource
    @Inject lateinit var mGalleryDataSource: GalleryDataSource

    @Inject lateinit var mNoteRepository: NoteRepository
    @Inject lateinit var mUserRepository: UserRepository

    private lateinit var mNotificationRepository: NotificationRepository

    private lateinit var mUserRepositoryEventToFlow: UserRepositoryEventToFlow

    @Inject lateinit var mSocketWithAccountProvider: SocketWithAccountProvider
    @Inject lateinit var mNoteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider

    @Inject lateinit var mChannelAPIWithAccountProvider: ChannelAPIWithAccountProvider

    @Inject lateinit var mNoteCaptureAPIAdapter: NoteCaptureAPIAdapter


    @Inject lateinit var mUnreadMessages: UnReadMessages

    private lateinit var mMessageRepository: MessageRepository
    private lateinit var mGroupRepository: GroupRepository

    private lateinit var mGetters: Getters

    private lateinit var mReactionHistoryPaginatorFactory: ReactionHistoryPaginator.Factory

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    private val mGalleryRepository: GalleryRepository by lazy {
        createGalleryRepository()
    }

    private val mFileUploaderProvider: FileUploaderProvider by lazy {
        OkHttpFileUploaderProvider(OkHttpClient(), this, GsonFactory.create(), getEncryption())
    }

    @Inject lateinit var mDriveFileRepository: DriveFileRepository

    @ExperimentalCoroutinesApi
    @FlowPreview
    override val messageObserver: MessageObserver by lazy {
        MessageObserver(
            accountRepository = getAccountRepository(),
            channelAPIProvider = mChannelAPIWithAccountProvider,
            getters = getGetters()
        )
    }


    @Inject lateinit var applicationScope: CoroutineScope

    @Inject lateinit var lf: Logger.Factory
    override val loggerFactory: Logger.Factory
        get() = lf
    private val logger: Logger by lazy {
        loggerFactory.create("MiApplication")
    }

    private lateinit var _networkState: Flow<Boolean>

    private val _taskExecutor: TaskExecutor by lazy {
        AppTaskExecutor(applicationScope + Dispatchers.IO, loggerFactory.create("TaskExecutor"))
    }

    private lateinit var _unreadNotificationDAO: UnreadNotificationDAO

    private val _subscribeRegistration: SubscriptionRegistration by lazy {
        SubscriptionRegistration(
            getAccountRepository(),
            getEncryption(),
            getMisskeyAPIProvider(),
            lang = Locale.getDefault().language,
            loggerFactory
        )
    }

    private val _subscriptionUnRegistration: SubscriptionUnRegistration by lazy {
        SubscriptionUnRegistration(
            getAccountRepository(),
            getEncryption(),
            lang = Locale.getDefault().language,
            misskeyAPIProvider = getMisskeyAPIProvider()
        )
    }

    private val noteTranslationStore: NoteTranslationStore by lazy {
        NoteTranslationStore(
            getNoteRepository(),
            getAccountRepository(),
            getMisskeyAPIProvider(),
            getEncryption(),
        )
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val config = BundledEmojiCompatConfig(this)
            .setReplaceAll(true)
        EmojiCompat.init(config)

        _networkState = activeNetworkFlow().shareIn(applicationScope, SharingStarted.Eagerly)

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)


        draftNoteDao = database.draftNoteDao()

        urlPreviewDAO = database.urlPreviewDAO()

        _unreadNotificationDAO = database.unreadNotificationDAO()

        metaRepository = MediatorMetaRepository(RoomMetaRepository(database.metaDAO(), database.emojiAliasDAO(), database), InMemoryMetaRepository())

        metaStore = MediatorMetaStore(metaRepository, RemoteMetaStore(), true, loggerFactory)

        mUserRepositoryEventToFlow = UserRepositoryEventToFlow(mUserDataSource, applicationScope, loggerFactory)




        mGroupRepository = GroupRepositoryImpl(
            misskeyAPIProvider = mMisskeyAPIProvider,
            accountRepository = mAccountRepository,
            groupDataSource = mGroupDataSource,
            encryption = mEncryption,
            loggerFactory.create("GroupRepositoryImpl")
        )


        mMessageRepository = MessageRepositoryImpl(this)

        mGetters = Getters(mNoteDataSource, mNoteRepository,mUserDataSource,mFilePropertyDataSource, mNotificationDataSource, mMessageDataSource, mGroupDataSource, loggerFactory)

        mNotificationRepository = NotificationRepositoryImpl(
            mNotificationDataSource,
            mSocketWithAccountProvider,
            mAccountRepository,
            getGetters().notificationRelationGetter,
            database.unreadNotificationDAO()
        )

        mReactionHistoryPaginatorFactory = ReactionHistoryPaginatorImpl.Factory(mReactionHistoryDataSource, mMisskeyAPIProvider, mAccountRepository, getEncryption(), mUserDataSource)

        val mainEventDispatcher = MediatorMainEventDispatcher.Factory(this).create()
        getCurrentAccount().filterNotNull().flatMapLatest { ac ->
            getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map { body ->
                ac to body
            }
        }.mapNotNull {
            (it.second as? ChannelBody.Main)?.let{ main ->
                it.first to main
            }
        }.onEach {
            mainEventDispatcher.dispatch(it.first, it.second)
        }.catch { e ->
            logger.error("Dispatchi時にエラー発生", e = e)
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

        _networkState.distinctUntilChanged().onEach {
            logger.debug("接続状態が変化:${if(it) "接続" else "未接続"}")
            mSocketWithAccountProvider.all().forEach { socket ->
                if(it) {
                    socket.onNetworkActive()
                }else{
                    socket.onNetworkInActive()
                }
            }

        }.catch { e ->
            logger.error("致命的なエラー", e)
        }.launchIn(applicationScope + Dispatchers.IO)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(!it.isSuccessful) {
                return@addOnCompleteListener
            }
            it.result?.also { token ->
                applicationScope.launch(Dispatchers.IO) {
                    runCatching {
                        getSubscriptionRegistration().registerAll(token)
                    }.onFailure { e ->
                        logger.error("register error", e)
                    }
                }
            }
        }.addOnFailureListener {
            logger.debug("fcm token取得失敗", e = it)
        }
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

    override fun getGroupDataSource(): GroupDataSource {
        return mGroupDataSource
    }

    override fun getGroupRepository(): GroupRepository {
        return mGroupRepository
    }

    override fun getUnreadMessages(): UnReadMessages {
        return mUnreadMessages
    }

    override fun getGetters(): Getters {
        return mGetters
    }

    override fun getDriveFileRepository(): DriveFileRepository {
        return mDriveFileRepository
    }

    override fun getUnreadNotificationDAO() = _unreadNotificationDAO

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

    override suspend fun setCurrentAccount(account: Account) {
        try{
            mAccountRepository.setCurrentAccount(account)
            loadAndInitializeAccounts()
        }catch(e: Exception){
            logger.error("switchAccount error", e)
        }
    }

    override suspend fun addAccount(account: Account) {
        try{
            mAccountRepository.add(account, true)

            loadAndInitializeAccounts()
        }catch(e: Exception){

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

    override fun getSocket(account: Account): Socket {
        return mSocketWithAccountProvider.get(account)
    }

    override fun getMetaStore(): MetaStore {
        return metaStore
    }

    override fun getFilePropertyDataSource(): FilePropertyDataSource {
        return mFilePropertyDataSource
    }

    override fun getFileUploaderProvider(): FileUploaderProvider {
        return mFileUploaderProvider
    }

    override fun getGalleryDataSource(): GalleryDataSource {
        return mGalleryDataSource
    }

    override fun getGalleryRepository(): GalleryRepository {
        return mGalleryRepository
    }

    override fun getSubscriptionRegistration(): SubscriptionRegistration {
        return _subscribeRegistration
    }

    override fun getSubscriptionUnRegstration(): SubscriptionUnRegistration {
        return _subscriptionUnRegistration
    }

    override fun getTranslationStore(): NoteTranslationStore {
        return noteTranslationStore
    }

    override fun getMetaRepository(): MetaRepository {
        return metaRepository
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
            val meta = metaStore.fetch(instanceDomain)

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


    override fun getMisskeyAPIProvider(): MisskeyAPIProvider {
        return mMisskeyAPIProvider
    }

    override fun getEncryption(): Encryption {
        return mEncryption
    }

    override fun getNoteCaptureAPI(account: Account): NoteCaptureAPI {
        return mNoteCaptureAPIWithAccountProvider.get(account)
    }

    override fun getReactionHistoryDataSource(): ReactionHistoryDataSource {
        return mReactionHistoryDataSource
    }

    override fun getReactionHistoryPaginatorFactory(): ReactionHistoryPaginator.Factory {
        return mReactionHistoryPaginatorFactory
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



    override fun getTaskExecutor(): TaskExecutor {
        return _taskExecutor
    }



}