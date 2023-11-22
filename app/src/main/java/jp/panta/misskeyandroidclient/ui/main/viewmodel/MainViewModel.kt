package jp.panta.misskeyandroidclient.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.emoji.EmojiEventHandler
import net.pantasystem.milktea.model.messaging.UnReadMessages
import net.pantasystem.milktea.model.notification.NotificationRepository
import net.pantasystem.milktea.model.notification.NotificationStreaming
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.statistics.InAppPostCounterRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val accountStore: AccountStore,
    unreadMessages: UnReadMessages,
    loggerFactory: Logger.Factory,
    private val notificationRepository: NotificationRepository,
    private val configRepository: LocalConfigRepository,
    private val emojiEventHandler: EmojiEventHandler,
    private val notificationStreaming: NotificationStreaming,
    inAppPostCounterRepository: InAppPostCounterRepository,
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
        notificationStreaming.connect { ac }
    }.flowOn(Dispatchers.IO).catch { e ->
        logger.error("通知取得エラー", e = e)
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val isShowFirebaseCrashlytics = settingStore.configState.map {
        it.isCrashlyticsCollectionEnabled
    }.map {
        !(it.isEnable || it.isConfirmed)
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    val isShowEnableSafeSearchDescription = settingStore.configState.map {
        it.isEnableSafeSearch
    }.map {
        !(!it.isEnabled || it.isConfirmed)
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    val isShowGoogleAnalyticsDialog = settingStore.configState.map { config ->
        !(config.isAnalyticsCollectionEnabled.isEnabled
                || config.isAnalyticsCollectionEnabled.isConfirmed)
                && config.isCrashlyticsCollectionEnabled.isConfirmed
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    val isRequestPushNotificationPermission = settingStore.configState.map { config ->
        !config.isConfirmedPostNotification
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val isShowInAppReview = combine(inAppPostCounterRepository.observe(), accountStore.observeAccounts) { count, accounts ->
        accounts.size >= 2 && count >= 10 || count > 50
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))


    val state: StateFlow<MainUiState> = combine(
        unreadNotificationCount,
        unreadMessageCount,
    ) { unc, umc->
        MainUiState(unc, umc)
    }.stateIn(viewModelScope, SharingStarted.Lazily, MainUiState())

    init {
        viewModelScope.launch {
            accountStore.observeCurrentAccount.collect(emojiEventHandler::observe)
        }
    }

    fun setCrashlyticsCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            configRepository.save(
                configRepository.get().getOrThrow().setCrashlyticsCollectionEnabled(enabled)
            ).getOrThrow()
        }
    }

    fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCancellableCatching {
                configRepository.save(
                    configRepository.get().getOrThrow().setAnalyticsCollectionEnabled(enabled)
                ).getOrThrow()
            }.onFailure {
                FirebaseCrashlytics.getInstance().recordException(it)
            }
        }
    }


    fun onPushNotificationConfirmed() {
        viewModelScope.launch {
            configRepository.get().mapCancellableCatching {
                configRepository.save(it.copy(isConfirmedPostNotification = true))
            }.onFailure {
                logger.error("設定状態の保存に失敗", it)
            }

        }
    }

    fun onDoNotShowSafeSearchDescription() {
        viewModelScope.launch {
            configRepository.get().mapCancellableCatching {
                configRepository.save(it.copy(isEnableSafeSearch = it.isEnableSafeSearch.copy(isConfirmed = true)))
            }.onFailure {
                logger.error("設定状態の保存に失敗", it)
            }
        }
    }

}

data class MainUiState (
    val unreadNotificationCount: Int = 0,
    val unreadMessagesCount: Int = 0,
)