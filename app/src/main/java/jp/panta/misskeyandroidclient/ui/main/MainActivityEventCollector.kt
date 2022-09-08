package jp.panta.misskeyandroidclient.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MainActivity
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import jp.panta.misskeyandroidclient.ui.notification.notificationMessageScope
import jp.panta.misskeyandroidclient.ui.strings_helper.webSocketStateMessageScope
import jp.panta.misskeyandroidclient.ui.users.ReportStateHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android_ui.report.ReportViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.model.TaskState
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.report.ReportState

internal class MainActivityEventCollector (
    val activity: MainActivity,
    val binding: ActivityMainBinding,
    val lifecycleScope: CoroutineScope,
    val lifecycleOwner: LifecycleOwner,
    val mainViewModel: MainViewModel,
    val reportViewModel: ReportViewModel,
    val noteTaskExecutor: CreateNoteTaskExecutor,
    val accountStore: AccountStore,
    val authorizationNavigation: AuthorizationNavigation,
    val requestPostNotificationsPermissionLauncher: ActivityResultLauncher<String>,
    val changeNavMenuVisibilityFromAPIVersion: ChangeNavMenuVisibilityFromAPIVersion,
){

    fun setup() {
        // NOTE: 各バージョンに合わせMenuを制御している
        mainViewModel.getCurrentAccountMisskeyAPI().filterNotNull().onEach { api ->
            changeNavMenuVisibilityFromAPIVersion(api)
        }.launchIn(lifecycleScope)


        mainViewModel.state.onEach { uiState ->
            ShowBottomNavigationBadgeDelegate(binding.appBarMain.bottomNavigation)(uiState)
        }.launchIn(lifecycleScope)


        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mainViewModel.currentAccountSocketStateEvent.collect {
                    activity.webSocketStateMessageScope {
                        it.showToastMessage()
                    }
                }
            }
        }

        collectLatestNotifications()
        collectCrashlyticsCollectionState()
        collectReportSendingState()
        collectCreateNoteState()
        collectUnauthorizedState()
        collectConfirmGoogleAnalyticsState()
        collectRequestPostNotificationState()
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
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                reportViewModel.state.distinctUntilChangedBy {
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
                noteTaskExecutor.tasks.collect { taskState ->
                    showCreateNoteTaskStatusSnackBar(taskState)
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
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
                ringtone.play()
            }
        }
    }

    private fun collectUnauthorizedState() {
        lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStore.state.collect {
                    if (it.isUnauthorized) {
                        activity.startActivity(
                            authorizationNavigation.newIntent(Unit)
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
                    if ( requestPermission &&
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

    private fun showSendReportStateFrom(state: ReportState) {
        ReportStateHandler().invoke(binding.appBarMain.simpleNotification, state)
    }

    private fun showCreateNoteTaskStatusSnackBar(taskState: TaskState<Note>) {
        ShowNoteCreationResultSnackBar(
            activity,
            noteTaskExecutor,
            binding.appBarMain.simpleNotification
        )(taskState)
    }
}