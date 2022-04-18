package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import net.pantasystem.milktea.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.model.*
import net.pantasystem.milktea.data.model.drive.*
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.notes.*
import net.pantasystem.milktea.data.model.notes.reaction.impl.ReactionHistoryPaginatorImpl
import net.pantasystem.milktea.data.model.notification.db.UnreadNotificationDAO
import net.pantasystem.milktea.data.model.settings.ColorSettingStore
import net.pantasystem.milktea.data.model.settings.SettingStore
import net.pantasystem.milktea.data.model.settings.UrlPreviewSourceSetting
import net.pantasystem.milktea.data.model.streaming.ChannelAPIMainEventDispatcherAdapter
import net.pantasystem.milktea.data.model.streaming.MediatorMainEventDispatcher
import net.pantasystem.milktea.data.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.model.sw.register.SubscriptionUnRegistration
import net.pantasystem.milktea.data.model.url.*
import net.pantasystem.milktea.data.model.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.data.streaming.notes.NoteCaptureAPI
import net.pantasystem.milktea.common.getPreferenceName
import jp.panta.misskeyandroidclient.util.platform.activeNetworkFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.model.messaging.impl.MessageObserver
import net.pantasystem.milktea.data.model.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

//基本的な情報はここを返して扱われる
@HiltAndroidApp
class MiApplication : Application(), MiCore {

    @Inject
    lateinit var database: DataBase

    @Inject
    lateinit var reactionUserSettingDao: net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao

    @Inject
    lateinit var mSettingStore: SettingStore

    @Inject
    lateinit var draftNoteDao: DraftNoteDao

    @Inject
    lateinit var urlPreviewDAO: UrlPreviewDAO

    @Inject
    lateinit var mAccountRepository: net.pantasystem.milktea.model.account.AccountRepository

    @Inject
    lateinit var mMetaRepository: net.pantasystem.milktea.model.instance.MetaRepository

    @Inject
    lateinit var mFetchMeta: net.pantasystem.milktea.model.instance.FetchMeta

    private lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var mAccountStore: net.pantasystem.milktea.model.account.AccountStore

    @Inject
    lateinit var mEncryption: Encryption

    @Inject
    lateinit var mMetaCache: net.pantasystem.milktea.model.instance.MetaCache

    @Inject
    lateinit var mMisskeyAPIProvider: net.pantasystem.milktea.api.misskey.MisskeyAPIProvider

    @Inject
    lateinit var mNoteDataSource: net.pantasystem.milktea.model.notes.NoteDataSource
    @Inject
    lateinit var mUserDataSource: net.pantasystem.milktea.model.user.UserDataSource
    @Inject
    lateinit var mNotificationDataSource: net.pantasystem.milktea.model.notification.NotificationDataSource
    @Inject
    lateinit var mMessageDataSource: MessageDataSource
    @Inject
    lateinit var mReactionHistoryDataSource: net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
    @Inject
    lateinit var mGroupDataSource: net.pantasystem.milktea.model.group.GroupDataSource
    @Inject
    lateinit var mFilePropertyDataSource: net.pantasystem.milktea.model.drive.FilePropertyDataSource
    @Inject
    lateinit var mGalleryDataSource: net.pantasystem.milktea.model.gallery.GalleryDataSource

    @Inject
    lateinit var mNoteRepository: net.pantasystem.milktea.model.notes.NoteRepository
    @Inject
    lateinit var mUserRepository: net.pantasystem.milktea.model.user.UserRepository

    @Inject
    lateinit var mNotificationRepository: net.pantasystem.milktea.model.notification.NotificationRepository

    private lateinit var mUserRepositoryEventToFlow: net.pantasystem.milktea.model.user.UserRepositoryEventToFlow

    @Inject
    lateinit var mSocketWithAccountProvider: SocketWithAccountProvider
    @Inject
    lateinit var mNoteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider

    @Inject
    lateinit var mChannelAPIWithAccountProvider: ChannelAPIWithAccountProvider

    @Inject
    lateinit var mNoteCaptureAPIAdapter: NoteCaptureAPIAdapter


    @Inject
    lateinit var mUnreadMessages: net.pantasystem.milktea.model.messaging.UnReadMessages

    @Inject
    lateinit var mMessageRepository: net.pantasystem.milktea.model.messaging.MessageRepository
    @Inject
    lateinit var mGroupRepository: net.pantasystem.milktea.model.group.GroupRepository

    @Inject
    lateinit var mGetters: Getters

    private lateinit var mReactionHistoryPaginatorFactory: net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator.Factory

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    lateinit var colorSettingStore: ColorSettingStore
        private set

    @Inject
    lateinit var mGalleryRepository: net.pantasystem.milktea.model.gallery.GalleryRepository

    @Inject
    lateinit var mFileUploaderProvider: FileUploaderProvider

    @Inject
    lateinit var mDriveFileRepository: net.pantasystem.milktea.model.drive.DriveFileRepository

    @Inject
    lateinit var mNoteReservationPostExecutor: net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor


    @Inject
    lateinit var noteTranslationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore

    @Inject
    lateinit var mainEventDispatcherFactory: MediatorMainEventDispatcher.Factory

    @Inject
    lateinit var channelAPIMainEventDispatcherAdapter: ChannelAPIMainEventDispatcherAdapter

    @ExperimentalCoroutinesApi
    @FlowPreview
    override val messageObserver: MessageObserver by lazy {
        MessageObserver(
            accountRepository = getAccountRepository(),
            channelAPIProvider = mChannelAPIWithAccountProvider,
            getters = getGetters()
        )
    }


    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var lf: net.pantasystem.milktea.common.Logger.Factory
    override val loggerFactory: net.pantasystem.milktea.common.Logger.Factory
        get() = lf
    private val logger: net.pantasystem.milktea.common.Logger by lazy {
        loggerFactory.create("MiApplication")
    }

    private lateinit var _networkState: Flow<Boolean>

    @Inject
    lateinit var mUnreadNotificationDAO: UnreadNotificationDAO

    private val _subscribeRegistration: SubscriptionRegistration by lazy {
        SubscriptionRegistration(
            getAccountRepository(),
            getEncryption(),
            getMisskeyAPIProvider(),
            lang = Locale.getDefault().language,
            loggerFactory,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
        )
    }

    private val _subscriptionUnRegistration: SubscriptionUnRegistration by lazy {
        SubscriptionUnRegistration(
            getAccountRepository(),
            getEncryption(),
            lang = Locale.getDefault().language,
            misskeyAPIProvider = getMisskeyAPIProvider(),
            endpointBase = BuildConfig.PUSH_TO_FCM_SERVER_BASE_URL,
            auth = BuildConfig.PUSH_TO_FCM_AUTH,
            publicKey = BuildConfig.PUSH_TO_FCM_PUBLIC_KEY,
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

        mUserRepositoryEventToFlow =
            net.pantasystem.milktea.model.user.UserRepositoryEventToFlow(
                mUserDataSource,
                applicationScope,
                loggerFactory
            )


        mReactionHistoryPaginatorFactory = ReactionHistoryPaginatorImpl.Factory(
            mReactionHistoryDataSource,
            mMisskeyAPIProvider,
            mAccountRepository,
            getEncryption(),
            mUserDataSource
        )

        val mainEventDispatcher = mainEventDispatcherFactory.create()
        channelAPIMainEventDispatcherAdapter(mainEventDispatcher)

        mAccountRepository.addEventListener { ev ->
            applicationScope.launch(Dispatchers.IO) {
                try {
                    if (ev is net.pantasystem.milktea.model.account.AccountRepository.Event.Deleted) {
                        mSocketWithAccountProvider.get(ev.accountId)?.disconnect()
                    }
                    mAccountStore.initialize()
                } catch (e: Exception) {
                    logger.error("アカウントの更新があったのでStateを更新しようとしたところ失敗しました。", e)
                }
            }
        }

        applicationScope.launch(Dispatchers.IO) {
            try {
                mAccountStore.initialize()
            } catch (e: Exception) {
                logger.error("load account error", e = e)
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesChangedListener)

        _networkState.distinctUntilChanged().onEach {
            logger.debug("接続状態が変化:${if (it) "接続" else "未接続"}")
            mSocketWithAccountProvider.all().forEach { socket ->
                if (it) {
                    socket.onNetworkActive()
                } else {
                    socket.onNetworkInActive()
                }
            }
        }.catch { e ->
            logger.error("致命的なエラー", e)
        }.launchIn(applicationScope + Dispatchers.IO)

        runCatching {
            FirebaseMessaging.getInstance()
        }.getOrNull()?.token?.addOnCompleteListener {
            if (!it.isSuccessful) {
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
        }?.addOnFailureListener {
            logger.debug("fcm token取得失敗", e = it)
        }

        mAccountStore.state.distinctUntilChangedBy { state ->
            state.accounts.map { it.accountId } to state.currentAccountId
        }.onEach {
            setUpMetaMap(it.accounts)
        }.launchIn(applicationScope + Dispatchers.IO)
    }


    override suspend fun getAccount(accountId: Long): net.pantasystem.milktea.model.account.Account {
        return mAccountRepository.get(accountId)
    }

    override fun getUrlPreviewStore(account: net.pantasystem.milktea.model.account.Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }

    override fun getAccountStore(): net.pantasystem.milktea.model.account.AccountStore {
        return mAccountStore
    }


    override fun getNoteCaptureAdapter(): NoteCaptureAPIAdapter {
        return mNoteCaptureAPIAdapter
    }

    override fun getAccountRepository(): net.pantasystem.milktea.model.account.AccountRepository {
        return mAccountRepository
    }

    override fun getNotificationDataSource(): net.pantasystem.milktea.model.notification.NotificationDataSource {
        return mNotificationDataSource
    }

    override fun getNotificationRepository(): net.pantasystem.milktea.model.notification.NotificationRepository {
        return mNotificationRepository
    }

    override fun getMessageDataSource(): MessageDataSource {
        return mMessageDataSource
    }

    override fun getMessageRepository(): net.pantasystem.milktea.model.messaging.MessageRepository {
        return mMessageRepository
    }

    override fun getGroupDataSource(): net.pantasystem.milktea.model.group.GroupDataSource {
        return mGroupDataSource
    }

    override fun getGroupRepository(): net.pantasystem.milktea.model.group.GroupRepository {
        return mGroupRepository
    }

    override fun getUnreadMessages(): net.pantasystem.milktea.model.messaging.UnReadMessages {
        return mUnreadMessages
    }

    override fun getGetters(): Getters {
        return mGetters
    }

    override fun getDriveFileRepository(): net.pantasystem.milktea.model.drive.DriveFileRepository {
        return mDriveFileRepository
    }

    override fun getUnreadNotificationDAO() = mUnreadNotificationDAO

    private fun getUrlPreviewStore(account: net.pantasystem.milktea.model.account.Account, isReplace: Boolean): UrlPreviewStore {
        return account.instanceDomain.let { accountUrl ->
            val url = mSettingStore.urlPreviewSetting.getSummalyUrl() ?: accountUrl

            var store = mUrlPreviewStoreInstanceBaseUrlMap[url]
            if (store == null || isReplace) {
                store = UrlPreviewStoreFactory(
                    urlPreviewDAO, mSettingStore.urlPreviewSetting.getSourceType(),
                    mSettingStore.urlPreviewSetting.getSummalyUrl(),
                    mAccountStore.state.value.currentAccount
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[url] = store
            store
        }
    }

    override suspend fun setCurrentAccount(account: net.pantasystem.milktea.model.account.Account) {
        try {
            mAccountRepository.setCurrentAccount(account)
            mAccountStore.initialize()
//            loadAndInitializeAccounts()
        } catch (e: Exception) {
            logger.error("switchAccount error", e)
        }
    }

    override fun getDraftNoteDAO(): DraftNoteDao {
        return draftNoteDao
    }


    override fun getSettingStore(): SettingStore {
        return this.mSettingStore
    }

    override fun getNoteDataSource(): net.pantasystem.milktea.model.notes.NoteDataSource {
        return mNoteDataSource
    }

    override fun getNoteRepository(): net.pantasystem.milktea.model.notes.NoteRepository {
        return mNoteRepository
    }

    override fun getUserDataSource(): net.pantasystem.milktea.model.user.UserDataSource {
        return mUserDataSource
    }

    override fun getUserRepository(): net.pantasystem.milktea.model.user.UserRepository {
        return mUserRepository
    }

    override fun getUserRepositoryEventToFlow(): net.pantasystem.milktea.model.user.UserRepositoryEventToFlow {
        return mUserRepositoryEventToFlow
    }

    override suspend fun getChannelAPI(account: net.pantasystem.milktea.model.account.Account): ChannelAPI {
        return mChannelAPIWithAccountProvider.get(account)
    }

    override fun getSocket(account: net.pantasystem.milktea.model.account.Account): Socket {
        return mSocketWithAccountProvider.get(account)
    }

    override fun getMetaStore(): net.pantasystem.milktea.model.instance.FetchMeta {
        return mFetchMeta
    }

    override fun getFilePropertyDataSource(): net.pantasystem.milktea.model.drive.FilePropertyDataSource {
        return mFilePropertyDataSource
    }

    override fun getFileUploaderProvider(): FileUploaderProvider {
        return mFileUploaderProvider
    }

    override fun getGalleryDataSource(): net.pantasystem.milktea.model.gallery.GalleryDataSource {
        return mGalleryDataSource
    }

    override fun getGalleryRepository(): net.pantasystem.milktea.model.gallery.GalleryRepository {
        return mGalleryRepository
    }

    override fun getSubscriptionRegistration(): SubscriptionRegistration {
        return _subscribeRegistration
    }

    override fun getSubscriptionUnRegstration(): SubscriptionUnRegistration {
        return _subscriptionUnRegistration
    }

    override fun getTranslationStore(): net.pantasystem.milktea.model.notes.NoteTranslationStore {
        return noteTranslationStore
    }

    override fun getMetaRepository(): net.pantasystem.milktea.model.instance.MetaRepository {
        return mMetaRepository
    }

    override fun getNoteReservationPostExecutor(): net.pantasystem.milktea.model.notes.reservation.NoteReservationPostExecutor {
        return mNoteReservationPostExecutor
    }


    override fun getCurrentInstanceMeta(): net.pantasystem.milktea.model.instance.Meta? {
        return mAccountStore.currentAccount?.instanceDomain?.let { url ->
            mMetaCache.get(url)
        }
    }

    private suspend fun setUpMetaMap(accounts: List<net.pantasystem.milktea.model.account.Account>) {
        coroutineScope {
            accounts.map { ac ->
                async {
                    loadInstanceMetaAndSetupAPI(ac.instanceDomain)
                }
            }.awaitAll()
        }
    }


    private suspend fun loadInstanceMetaAndSetupAPI(instanceDomain: String) {
        try {
            val meta = mFetchMeta.fetch(instanceDomain, isForceFetch = true)
            mMetaCache.put(instanceDomain, meta)
        } catch (e: Exception) {
            logger.error("metaの読み込み一連処理に失敗したでち", e)
        }
    }


    override fun getMisskeyAPIProvider(): net.pantasystem.milktea.api.misskey.MisskeyAPIProvider {
        return mMisskeyAPIProvider
    }

    override fun getEncryption(): Encryption {
        return mEncryption
    }

    override fun getNoteCaptureAPI(account: net.pantasystem.milktea.model.account.Account): NoteCaptureAPI {
        return mNoteCaptureAPIWithAccountProvider.get(account)
    }

    override fun getReactionHistoryDataSource(): net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource {
        return mReactionHistoryDataSource
    }

    override fun getReactionHistoryPaginatorFactory(): net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator.Factory {
        return mReactionHistoryPaginatorFactory
    }

    private val sharedPreferencesChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                UrlPreviewSourceSetting.URL_PREVIEW_SOURCE_TYPE_KEY -> {
                    mAccountStore.state.value.accounts.forEach {
                        getUrlPreviewStore(it, true)
                    }
                }
            }
        }

}