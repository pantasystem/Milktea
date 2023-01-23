package jp.panta.misskeyandroidclient.ui.main

import android.Manifest
import android.content.pm.PackageManager
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
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.report.ReportViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.model.user.report.ReportState
import net.pantasystem.milktea.notification.notificationMessageScope
import net.pantasystem.milktea.user.ReportStateHandler
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor

internal class MainActivityEventHandler(
    val activity: MainActivity,
    val binding: ActivityMainBinding,
    val lifecycleScope: CoroutineScope,
    val lifecycleOwner: LifecycleOwner,
    val mainViewModel: MainViewModel,
    val reportViewModel: ReportViewModel,
    private val createNoteWorkerExecutor: CreateNoteWorkerExecutor,
    val accountStore: AccountStore,
    val authorizationNavigation: AuthorizationNavigation,
    val requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>,
    val changeNavMenuVisibilityFromAPIVersion: ChangeNavMenuVisibilityFromAPIVersion,
    private val configStore: SettingStore,
    private val draftNoteService: DraftNoteService,
) {


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
    }

    private fun collectCrashlyticsCollectionState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mainViewModel.isShowFirebaseCrashlytics.collect {
                    if (it) {
                        ConfirmCrashlyticsDialog().show(
                            activity.supportFragmentManager,
                            "confirm_crashlytics_dialog"
                        )
                    }
                }
            }
        }
    }

    private fun collectConfirmGoogleAnalyticsState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mainViewModel.isShowGoogleAnalyticsDialog.collect {
                    if (it) {
                        ConfirmGoogleAnalyticsDialog().show(
                            activity.supportFragmentManager,
                            "confirm_google_analytics_dialog"
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
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
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
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(activity, uri)

        // NOTE: 最新の通知をSnackBar等に表示する
        lifecycleScope.launch {
            lifecycleOwner.whenCreated {
                mainViewModel.newNotifications.collect { notificationRelation ->
                    activity.apply {
                        notificationMessageScope {
                            notificationRelation.showSnackBarMessage(binding.appBarMain.simpleNotification)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            lifecycleOwner.whenResumed {
                // NOTE: 通知音を再生する
                mainViewModel.newNotifications.collect {
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                    }
                    if (configStore.configState.value.isEnableNotificationSound) {
                        ringtone.play()
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
                            it.instanceDomain.substring(0, 36.coerceAtMost(it.instanceDomain.length))
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
                            authorizationNavigation.newIntent(AuthorizationArgs.New)
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
        ShowNoteCreationResultSnackBar(
            activity,
            binding.appBarMain.simpleNotification,
            createNoteWorkerExecutor,
        )(state)
    }
}