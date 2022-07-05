package jp.panta.misskeyandroidclient

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import jp.panta.misskeyandroidclient.util.platform.activeNetworkFlow
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.getPreferenceName
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.drive.ClearUnUsedDriveFileCacheJob
import net.pantasystem.milktea.data.infrastructure.drive.FileUploaderProvider
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDao
import net.pantasystem.milktea.data.infrastructure.settings.ColorSettingStore
import net.pantasystem.milktea.data.infrastructure.settings.Keys
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.settings.str
import net.pantasystem.milktea.data.infrastructure.streaming.ChannelAPIMainEventDispatcherAdapter
import net.pantasystem.milktea.data.infrastructure.streaming.MediatorMainEventDispatcher
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreProvider
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaCache
import net.pantasystem.milktea.model.messaging.UnReadMessages
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

//基本的な情報はここを返して扱われる
@HiltAndroidApp
class MiApplication : Application(), MiCore {

    @Inject
    lateinit var database: DataBase

    @Inject
    lateinit var reactionUserSettingDao: ReactionUserSettingDao

    @Inject
    lateinit var mSettingStore: SettingStore

    @Inject
    lateinit var draftNoteDao: DraftNoteDao

    @Inject
    lateinit var urlPreviewDAO: UrlPreviewDAO

    @Inject
    lateinit var mAccountRepository: AccountRepository


    @Inject
    lateinit var mFetchMeta: FetchMeta

    private lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var mAccountStore: AccountStore


    @Inject
    lateinit var mMetaCache: MetaCache

    @Inject
    lateinit var mMisskeyAPIProvider: MisskeyAPIProvider

    @Inject
    lateinit var mNoteDataSource: NoteDataSource

    @Inject
    lateinit var mUserDataSource: UserDataSource

    @Inject
    lateinit var mNotificationDataSource: NotificationDataSource

    @Inject
    lateinit var mMessageDataSource: MessageDataSource

    @Inject
    lateinit var mReactionHistoryDataSource: ReactionHistoryDataSource


    @Inject
    lateinit var mNoteRepository: NoteRepository


    @Inject
    lateinit var mSocketWithAccountProvider: SocketWithAccountProvider


    @Inject
    lateinit var mUnreadMessages: UnReadMessages


    @Inject
    lateinit var mGroupRepository: GroupRepository

    @Inject
    lateinit var urlPreviewProvider: UrlPreviewStoreProvider

    lateinit var colorSettingStore: ColorSettingStore
        private set


    @Inject
    lateinit var mFileUploaderProvider: FileUploaderProvider


    @Inject
    lateinit var mainEventDispatcherFactory: MediatorMainEventDispatcher.Factory

    @Inject
    lateinit var channelAPIMainEventDispatcherAdapter: ChannelAPIMainEventDispatcherAdapter


    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var lf: Logger.Factory

    private val logger: Logger by lazy {
        lf.create("MiApplication")
    }

    @Inject
    lateinit var clearDriveCacheJob: ClearUnUsedDriveFileCacheJob


    @Inject
    lateinit var mSubscriptionRegistration: SubscriptionRegistration


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        val config = BundledEmojiCompatConfig(this)
            .setReplaceAll(true)
        EmojiCompat.init(config)

        sharedPreferences = getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE)
        colorSettingStore = ColorSettingStore(sharedPreferences)

        val mainEventDispatcher = mainEventDispatcherFactory.create()
        channelAPIMainEventDispatcherAdapter(mainEventDispatcher)

        mAccountRepository.addEventListener { ev ->
            applicationScope.launch(Dispatchers.IO) {
                try {
                    if (ev is AccountRepository.Event.Deleted) {
                        mSocketWithAccountProvider.get(ev.accountId)?.disconnect()
                    }
                    mAccountStore.initialize()
                } catch (e: Exception) {
                    logger.error("アカウントの更新があったのでStateを更新しようとしたところ失敗しました。", e)
                }
            }
        }

        applicationScope.launch(Dispatchers.IO) {
            clearDriveCacheJob.checkAndClear().onFailure {
                logger.error("ドライブのキャッシュのクリーンアップに失敗しました", it)
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

        activeNetworkFlow().distinctUntilChanged().onEach {
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
                        mSubscriptionRegistration.registerAll(token)
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


    override fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return urlPreviewProvider.getUrlPreviewStore(account, false)
    }


    override fun getSubscriptionRegistration(): SubscriptionRegistration {
        return mSubscriptionRegistration
    }

    override fun getSettingStore(): SettingStore {
        return this.mSettingStore
    }




    override fun getCurrentInstanceMeta(): Meta? {
        return mAccountStore.currentAccount?.instanceDomain?.let { url ->
            mMetaCache.get(url)
        }
    }

    private suspend fun setUpMetaMap(accounts: List<Account>) {
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


    override fun getReactionHistoryDataSource(): ReactionHistoryDataSource {
        return mReactionHistoryDataSource
    }


    private val sharedPreferencesChangedListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                Keys.UrlPreviewSourceType.str() -> {
                    mAccountStore.state.value.accounts.forEach {
                        urlPreviewProvider.getUrlPreviewStore(it, true)
                    }
                }
            }
        }

}