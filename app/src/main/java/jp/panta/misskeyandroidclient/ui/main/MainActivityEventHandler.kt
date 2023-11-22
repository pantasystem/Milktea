package jp.panta.misskeyandroidclient.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.auth.JoinMilkteaActivity
import net.pantasystem.milktea.common_android_ui.report.ReportViewModel
import net.pantasystem.milktea.common_viewmodel.CurrentPageType
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.note.draft.DraftNoteService
import net.pantasystem.milktea.model.notification.Notification
import net.pantasystem.milktea.model.user.report.ReportState
import net.pantasystem.milktea.notification.notificationMessageScope
import net.pantasystem.milktea.user.ReportStateHandler
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
import javax.inject.Inject

internal class MainActivityEventHandler(
    val activity: MainActivity,
    val binding: ActivityMainBinding,
    private val lifecycleScope: CoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val mainViewModel: MainViewModel,
    val reportViewModel: ReportViewModel,
    private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
    val accountStore: AccountStore,
    private val requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>,
    val changeNavMenuVisibilityFromAPIVersion: ChangeNavMenuVisibilityFromAPIVersion,
    private val configStore: SettingStore,
    private val draftNoteService: DraftNoteService,
    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel,
) {

    class Factory @Inject constructor(
        private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
        val accountStore: AccountStore,
        private val configStore: SettingStore,
        private val draftNoteService: DraftNoteService,
        private val featureEnables: FeatureEnables,
    ) {
        fun create(
            activity: MainActivity,
            binding: ActivityMainBinding,
            mainViewModel: MainViewModel,
            reportViewModel: ReportViewModel,
            requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>,
            currentPageableTimelineViewModel: CurrentPageableTimelineViewModel,
        ): MainActivityEventHandler {
            return MainActivityEventHandler(
                activity,
                binding,
                activity.lifecycleScope,
                activity,
                mainViewModel,
                reportViewModel,
                createNoteWorkerExecutor,
                accountStore,
                requestPostNotificationsPermissionLauncher,
                ChangeNavMenuVisibilityFromAPIVersion(binding.navView, featureEnables),
                configStore,
                draftNoteService,
                currentPageableTimelineViewModel
            )
        }
    }


    fun setup() {
        // NOTE: 各バージョンに合わせMenuを制御している
        accountStore.observeCurrentAccount.filterNotNull().onEach {
            changeNavMenuVisibilityFromAPIVersion(it)
        }.catch {
            Log.e("MainActivity", "check version error", it)
        }.launchIn(lifecycleScope)


        mainViewModel.state.onEach { uiState ->
            ShowBottomNavigationBadgeDelegate(binding.appBarMain.bottomNavigation)(uiState)
        }.launchIn(lifecycleScope)

        collectLatestNotifications()
        collectCrashlyticsCollectionState()
        collectReportSendingState()
        collectCreateNoteState()
        collectUnauthorizedState()
        collectConfirmGoogleAnalyticsState()
        collectRequestPostNotificationState()
        collectDraftNoteSavedEvent()
        collectCurrentPageableState()
        collectEnableSafeSearchDescriptionState()
    }

    private fun collectCrashlyticsCollectionState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.isShowFirebaseCrashlytics.collect {
                    if (it && activity.supportFragmentManager.findFragmentByTag(
                            ConfirmCrashlyticsDialog.FRAGMENT_TAG
                        ) == null
                    ) {
                        ConfirmCrashlyticsDialog().show(
                            activity.supportFragmentManager,
                            ConfirmCrashlyticsDialog.FRAGMENT_TAG
                        )
                    }
                }
            }
        }
    }

    private fun collectEnableSafeSearchDescriptionState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.isShowEnableSafeSearchDescription.collect {
                    if (it && activity.supportFragmentManager.findFragmentByTag(
                            SafeSearchDescriptionDialog.TAG
                        ) == null
                    ) {
                        SafeSearchDescriptionDialog().show(
                            activity.supportFragmentManager,
                            SafeSearchDescriptionDialog.TAG
                        )
                    }
                }
            }
        }
    }

    private fun collectConfirmGoogleAnalyticsState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.isShowGoogleAnalyticsDialog.collect {
                    if (it && activity.supportFragmentManager.findFragmentByTag(
                            ConfirmGoogleAnalyticsDialog.FRAGMENT_TAG
                        ) == null
                    ) {
                        ConfirmGoogleAnalyticsDialog().show(
                            activity.supportFragmentManager,
                            ConfirmGoogleAnalyticsDialog.FRAGMENT_TAG
                        )
                    }
                }
            }
        }
    }

    private fun collectReportSendingState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                reportViewModel.successOrFailureEvent.distinctUntilChangedBy {
                    it is ReportState.Sending.Success
                            || it is ReportState.Sending.Failed
                }.collect { state ->
                    showSendReportStateFrom(state)
                }
            }

        }
    }

    private fun collectCreateNoteState() {
        // NOTE: ノート作成処理の状態をSnackBarで表示する
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                createNoteWorkerExecutor.getCreateNoteWorkInfosInAppActives()
                    .collect { workInfoList ->
                        Log.d("collectCreateNoteState", "workInfoList:$workInfoList")
                        workInfoList.forEach {
                            showCreateNoteTaskStatusSnackBar(it)
                        }
                    }
            }
        }
    }

    private fun collectLatestNotifications() {
        // NOTE: 最新の通知をSnackBar等に表示する
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.newNotifications.collect { notificationRelation ->
                    activity.apply {
                        notificationMessageScope {
                            notificationRelation.showSnackBarMessage(binding.appBarMain.simpleNotification)
                        }
                    }
                }
            }
        }
        val audioManager = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        var replayedNotifyId: Notification.Id? = null
        var ringtone: Ringtone? = null
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                withContext(Dispatchers.Default) {
                    if (ringtone == null) {
                        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        ringtone = RingtoneManager.getRingtone(activity, uri)
                    }
                }
                mainViewModel.newNotifications.collect {
                    if (replayedNotifyId == it.notification.id) {
                        return@collect
                    }
                    replayedNotifyId = it.notification.id
                    if (ringtone?.isPlaying == true) {
                        ringtone?.stop()
                    }
                    if (
                        configStore.configState.value.isEnableNotificationSound
                        && audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
                    ) {
                        ringtone?.play()
                    }
                }
            }
        }
    }

    private fun collectUnauthorizedState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStore.state.collect { state ->
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "CURRENT_ACCOUNT_USER_ID",
                        state.currentAccount?.remoteId ?: ""
                    )
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "CURRENT_INSTANCE_DOMAIN",
                        state.currentAccount?.instanceDomain ?: ""
                    )
                    FirebaseCrashlytics.getInstance().setCustomKey(
                        "CURRENT_USERNAME",
                        state.currentAccount?.userName ?: ""
                    )
                    FirebaseAnalytics.getInstance(activity).setUserProperty(
                        "CURRENT_ACCOUNT_USER_ID",
                        state.currentAccount?.let {
                            it.remoteId.substring(0, 36.coerceAtMost(it.remoteId.length))
                        }
                    )
                    FirebaseAnalytics.getInstance(activity).setUserProperty(
                        "CURRENT_INSTANCE_DOMAIN",
                        state.currentAccount?.let {
                            it.instanceDomain.substring(
                                0,
                                36.coerceAtMost(it.instanceDomain.length)
                            )
                        }
                    )
                    FirebaseAnalytics.getInstance(activity).setUserProperty(
                        "CURRENT_USERNAME",
                        state.currentAccount?.let {
                            it.userName.substring(0, 36.coerceAtMost(it.userName.length))
                        }
                    )
                    if (state.isUnauthorized) {
                        activity.startActivity(
                            Intent(activity, JoinMilkteaActivity::class.java)
                        )
                        activity.finish()
                    }
                }
            }
        }
    }

    private fun collectRequestPostNotificationState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mainViewModel.isRequestPushNotificationPermission.collect { requestPermission ->
                    if (requestPermission &&
                        ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_DENIED
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        requestPostNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectDraftNoteSavedEvent() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
                    draftNoteService.getDraftNoteSavedEventBy(it.accountId)
                }.collect(
                    ShowRequestSchedulePostResultSnackBar(
                        activity,
                        binding.appBarMain.simpleNotification
                    )::invoke
                )
            }
        }
    }

    private fun showSendReportStateFrom(state: ReportState) {
        ReportStateHandler().invoke(binding.appBarMain.simpleNotification, state)
    }

    private fun showCreateNoteTaskStatusSnackBar(state: WorkInfo) {
        NoteCreateResultHandler(
            activity,
            binding.appBarMain.simpleNotification,
            createNoteWorkerExecutor,
        )(state)
    }

    private fun collectCurrentPageableState() {
        currentPageableTimelineViewModel.currentType.onEach {
            binding.appBarMain.fab.setImageResource(
                when (it) {
                    CurrentPageType.Account -> {
                        R.drawable.ic_person_add_black_24dp
                    }

                    is CurrentPageType.Page -> {
                        R.drawable.ic_edit_black_24dp
                    }
                }
            )

        }.flowWithLifecycle(activity.lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)
    }
}