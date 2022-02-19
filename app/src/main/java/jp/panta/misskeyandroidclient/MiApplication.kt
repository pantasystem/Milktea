package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.gettters.Getters
import jp.panta.misskeyandroidclient.model.*
import jp.panta.misskeyandroidclient.model.account.*
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.gallery.GalleryDataSource
import jp.panta.misskeyandroidclient.model.gallery.GalleryRepository
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.group.GroupRepository
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.MetaRepository
import jp.panta.misskeyandroidclient.model.instance.FetchMeta
import jp.panta.misskeyandroidclient.model.instance.MetaCache
import jp.panta.misskeyandroidclient.model.messaging.MessageObserver
import jp.panta.misskeyandroidclient.model.messaging.MessageRepository
import jp.panta.misskeyandroidclient.model.messaging.UnReadMessages
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.notes.reaction.history.ReactionHistoryDao
import jp.panta.misskeyandroidclient.model.notes.reaction.impl.ReactionHistoryPaginatorImpl
import jp.panta.misskeyandroidclient.model.notes.reaction.usercustom.ReactionUserSettingDao
import jp.panta.misskeyandroidclient.model.notes.reservation.NoteReservationPostExecutor
import jp.panta.misskeyandroidclient.model.notification.NotificationDataSource
import jp.panta.misskeyandroidclient.model.notification.NotificationRepository
import jp.panta.misskeyandroidclient.model.notification.db.UnreadNotificationDAO
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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.ArrayList

//基本的な情報はここを返して扱われる
@HiltAndroidApp
class MiApplication : Application(), MiCore {

    @Inject lateinit var database: DataBase

    @Inject lateinit var reactionUserSettingDao: ReactionUserSettingDao

    @Inject lateinit var mSettingStore: SettingStore

    @Inject lateinit var draftNoteDao: DraftNoteDao

    @Inject lateinit var urlPreviewDAO: UrlPreviewDAO

    @Inject lateinit var mAccountRepository: AccountRepository

    @Inject lateinit var mMetaRepository: MetaRepository

    @Inject lateinit var mFetchMeta: FetchMeta

    private lateinit var sharedPreferences: SharedPreferences

    @Inject lateinit var mAccountStore: AccountStore

    @Inject lateinit var mEncryption: Encryption

    @Inject lateinit var mMetaCache: MetaCache

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

    @Inject lateinit var mNotificationRepository: NotificationRepository

    private lateinit var mUserRepositoryEventToFlow: UserRepositoryEventToFlow

    @Inject lateinit var mSocketWithAccountProvider: SocketWithAccountProvider
    @Inject lateinit var mNoteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider

    @Inject lateinit var mChannelAPIWithAccountProvider: ChannelAPIWithAccountProvider

    @Inject lateinit var mNoteCaptureAPIAdapter: NoteCaptureAPIAdapter


    @Inject lateinit var mUnreadMessages: UnReadMessages

    @Inject lateinit var mMessageRepository: MessageRepository
    @Inject lateinit var mGroupRepository: GroupRepository

    @Inject lateinit var mGetters: Getters

    private lateinit var mReactionHistoryPaginatorFactory: ReactionHistoryPaginator.Factory

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    @Inject lateinit var mGalleryRepository: GalleryRepository

    @Inject lateinit var mFileUploaderProvider: FileUploaderProvider

    @Inject lateinit var mDriveFileRepository: DriveFileRepository

    @Inject
    lateinit var mNoteReservationPostExecutor: NoteReservationPostExecutor


    @Inject
    lateinit var noteTranslationStore: NoteTranslationStore

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

    @Inject lateinit var mUnreadNotificationDAO: UnreadNotificationDAO

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


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val config = BundledEmojiCompatConfig(this)
            .setReplaceAll(true)
        EmojiCompat.init(config)

        _networkState = activeNetworkFlow().shareIn(applicationScope, SharingStarted.Eagerly)

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)

        mUserRepositoryEventToFlow = UserRepositoryEventToFlow(mUserDataSource, applicationScope, loggerFactory)


        mReactionHistoryPaginatorFactory = ReactionHistoryPaginatorImpl.Factory(mReactionHistoryDataSource, mMisskeyAPIProvider, mAccountRepository, getEncryption(), mUserDataSource)

        val mainEventDispatcher = MediatorMainEventDispatcher.Factory(this).create()
        getAccountStore().observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
            getChannelAPI(ac).connect(ChannelAPI.Type.Main).map { body ->
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
                    mAccountStore.initialize()
                }catch(e: Exception) {
                    logger.error("アカウントの更新があったのでStateを更新しようとしたところ失敗しました。", e)
                }
            }
        }

        applicationScope.launch(Dispatchers.IO){
            try{
                //val connectionInstances = connectionInstanceDao!!.findAll()
                AccountMigration(database.accountDao(), mAccountRepository, sharedPreferences).executeMigrate()
                mAccountStore.initialize()
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
        mAccountStore.state.onEach {
            setUpMetaMap(it.accounts)
        }.launchIn(applicationScope + Dispatchers.IO)
    }



    override suspend fun getAccount(accountId: Long): Account {
        return mAccountRepository.get(accountId)
    }

    override fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }

    override fun getAccountStore(): AccountStore {
        return mAccountStore
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

    override fun getUnreadNotificationDAO() = mUnreadNotificationDAO

    private fun getUrlPreviewStore(account: Account, isReplace: Boolean): UrlPreviewStore{
        return account.instanceDomain.let{ accountUrl ->
            val url = mSettingStore.urlPreviewSetting.getSummalyUrl()?: accountUrl

            var store = mUrlPreviewStoreInstanceBaseUrlMap[url]
            if(store == null || isReplace){
                store = UrlPreviewStoreFactory(
                    urlPreviewDAO
                    ,mSettingStore.urlPreviewSetting.getSourceType(),
                    mSettingStore.urlPreviewSetting.getSummalyUrl(),
                    mAccountStore.state.value.currentAccount
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[url] = store
            store
        }
    }

    override suspend fun setCurrentAccount(account: Account) {
        try{
            mAccountRepository.setCurrentAccount(account)
            mAccountStore.initialize()
//            loadAndInitializeAccounts()
        }catch(e: Exception){
            logger.error("switchAccount error", e)
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

    override fun getMetaStore(): FetchMeta {
        return mFetchMeta
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
        return mMetaRepository
    }

    override fun getNoteReservationPostExecutor(): NoteReservationPostExecutor {
        return mNoteReservationPostExecutor
    }



    override fun getCurrentInstanceMeta(): Meta?{
        return mAccountStore.currentAccount?.instanceDomain?.let{ url ->
            mMetaCache.get(url)
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
        return try{
            val meta = mFetchMeta.fetch(instanceDomain)

            mMetaCache.put(instanceDomain, meta)

            meta

        }catch(e: Exception){
            logger.error("metaの読み込み一連処理に失敗したでち", e)
            null
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
                    mAccountStore.state.value.accounts.forEach {
                        getUrlPreviewStore(it, true)
                    }
                }
            }
        }


    override fun getTaskExecutor(): TaskExecutor {
        return _taskExecutor
    }



}