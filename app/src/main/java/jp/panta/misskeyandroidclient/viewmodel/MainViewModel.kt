package jp.panta.misskeyandroidclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.BuildConfig
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.streaming.stateEvent
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.messaging.UnReadMessages
import net.pantasystem.milktea.model.notification.NotificationRepository
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val accountStore: AccountStore,
    unreadMessages: UnReadMessages,
    loggerFactory: Logger.Factory,
    private val notificationRepository: NotificationRepository,
    private val getters: Getters,
    private val channelAPIProvider: ChannelAPIWithAccountProvider,
    private val socketProvider: SocketWithAccountProvider,
    private val configRepository: LocalConfigRepository,
    settingStore: SettingStore
) : ViewModel() {
    val logger by lazy {
        loggerFactory.create("MainViewModel")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadMessageCount = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        unreadMessages.findByAccountId(it.accountId)
    }.map {
        it.size
    }.flowOn(Dispatchers.IO).catch { e ->
        logger.error("メッセージ既読数取得エラー", e = e)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotificationCount = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        notificationRepository.countUnreadNotification(it.accountId)
    }.flowOn(Dispatchers.IO).catch { e ->
        logger.error("通知既読数取得エラー", e = e)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val newNotifications = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { ac ->
        channelAPIProvider.get(ac).connect(ChannelAPI.Type.Main).map { body ->
            body as? ChannelBody.Main.Notification
        }.filterNotNull().map {
            ac to it
        }
    }.map {
        getters.notificationRelationGetter.get(it.first, it.second.body)
    }.flowOn(Dispatchers.IO).catch { e ->
        logger.error("通知取得エラー", e = e)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val isShowFirebaseCrashlytics = settingStore.configState.map {
        it.isCrashlyticsCollectionEnabled
    }.map {
        !(it.isEnable || it.isConfirmed)
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    val isShowGoogleAnalyticsDialog = settingStore.configState.map { config ->
        !(config.isAnalyticsCollectionEnabled.isEnabled
                || config.isAnalyticsCollectionEnabled.isConfirmed)
                && config.isCrashlyticsCollectionEnabled.isConfirmed
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccountSocketStateEvent =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            socketProvider.get(it).stateEvent()
        }.filter { BuildConfig.DEBUG }.catch { e ->
            logger.error("WebSocket　状態取得エラー", e)
        }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())


    val state: StateFlow<MainUiState> = combine(
        unreadNotificationCount,
        unreadMessageCount,
    ) { unc, umc->
        MainUiState(unc, umc)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MainUiState())

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            configRepository.save(
                configRepository.get().getOrThrow().setCrashlyticsCollectionEnabled(enabled)
            ).getOrThrow()
        }
    }

    fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                configRepository.save(
                    configRepository.get().getOrThrow().setAnalyticsCollectionEnabled(enabled)
                ).getOrThrow()
            }.onFailure {
                FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
    }
}

data class MainUiState (
    val unreadNotificationCount: Int = 0,
    val unreadMessagesCount: Int = 0,
)