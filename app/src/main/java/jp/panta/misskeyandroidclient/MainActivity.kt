package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.main.*
import jp.panta.misskeyandroidclient.ui.notes.view.ActionNoteHandler
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notification.notificationMessageScope
import jp.panta.misskeyandroidclient.ui.strings_helper.webSocketStateMessageScope
import jp.panta.misskeyandroidclient.ui.users.ReportStateHandler
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ReportViewModel
import jp.panta.misskeyandroidclient.util.DoubleBackPressedFinishDelegate
import jp.panta.misskeyandroidclient.viewmodel.MainViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.SuitableType
import net.pantasystem.milktea.common_viewmodel.suitableType
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.gallery.GalleryPostsActivity
import net.pantasystem.milktea.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.model.TaskState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.report.ReportState
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarSetter {

    val mNotesViewModel: NotesViewModel by viewModels()

    private val mAccountViewModel: AccountViewModel by viewModels()

    private val mBackPressedDelegate = DoubleBackPressedFinishDelegate()

    @Inject
    lateinit var loggerFactory: Logger.Factory
    private val logger: Logger by lazy {
        loggerFactory.create("MainActivity")
    }


    private val binding: ActivityMainBinding by dataBinding()

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var noteTaskExecutor: CreateNoteTaskExecutor

    @Inject
    lateinit var metaRepository: MetaRepository

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var setTheme: ApplyTheme


    private val mainViewModel: MainViewModel by viewModels()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by viewModels()

    private val reportViewModel: ReportViewModel by viewModels()

    private lateinit var toggleNavigationDrawerDelegate: ToggleNavigationDrawerDelegate

    private lateinit var showBottomNavBadgeCountDelegate: ShowBottomNavigationBadgeDelegate

    private lateinit var showNoteCreationResultSnackBar: ShowNoteCreationResultSnackBar

    private lateinit var changeNavMenuVisibilityFromAPIVersion: ChangeNavMenuVisibilityFromAPIVersion


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        setContentView(R.layout.activity_main)


        toggleNavigationDrawerDelegate = ToggleNavigationDrawerDelegate(this, binding.drawerLayout)
        showBottomNavBadgeCountDelegate =
            ShowBottomNavigationBadgeDelegate(binding.appBarMain.bottomNavigation)
        showNoteCreationResultSnackBar = ShowNoteCreationResultSnackBar(
            this,
            noteTaskExecutor,
            binding.appBarMain.simpleNotification
        )

        changeNavMenuVisibilityFromAPIVersion =
            ChangeNavMenuVisibilityFromAPIVersion(binding.navView)

        binding.navView.setNavigationItemSelectedListener { item ->
            StartActivityFromNavDrawerItems(this).showNavDrawersActivityBy(item)
            binding.drawerLayout.closeDrawerWhenOpened()
            false
        }

        binding.appBarMain.fab.setOnClickListener {
            onFabClicked()
        }

        initAccountViewModelListener()
        SetUpNavHeader(binding.navView, this, mAccountViewModel).invoke()

        ActionNoteHandler(
            this,
            mNotesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore,
        ).initViewModelListener()


        // NOTE: 各ばーしょんに合わせMenuを制御している
        getCurrentAccountMisskeyAPI().filterNotNull().onEach { api ->
            changeNavMenuVisibilityFromAPIVersion(api)
        }.launchIn(lifecycleScope)


        lifecycleScope.launchWhenStarted {
            accountStore.state.collect {
                if (it.isUnauthorized) {
                    this@MainActivity.startActivity(
                        authorizationNavigation.newIntent(Unit)
                    )
                    finish()
                }
            }
        }

        mainViewModel.state.onEach { uiState ->
            showBottomNavBadgeCountDelegate(uiState)
        }.launchIn(lifecycleScope)

        // NOTE: 最新の通知をSnackBar等に表示する
        mainViewModel.newNotifications.onEach { notificationRelation ->
            notificationMessageScope {
                notificationRelation.showSnackBarMessage(binding.appBarMain.simpleNotification)
            }
        }.catch { e ->
            logger.error("通知取得エラー", e = e)
        }.launchIn(lifecycleScope + Dispatchers.Main)

        lifecycleScope.launchWhenResumed {
            mainViewModel.currentAccountSocketStateEvent.collect {
                webSocketStateMessageScope {
                    it.showToastMessage()
                }
            }
        }

        // NOTE: ノート作成処理の状態をSnackBarで表示する
        lifecycleScope.launchWhenCreated {
            noteTaskExecutor.tasks.collect { taskState ->
                showCreateNoteTaskStatusSnackBar(taskState)
            }
        }

        lifecycleScope.launchWhenResumed {
            reportViewModel.state.distinctUntilChangedBy {
                it is ReportState.Sending.Success
                        || it is ReportState.Sending.Failed
            }.collect { state ->
                showSendReportStateFrom(state)
            }
        }

        startService(Intent(this, NotificationService::class.java))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.contentMain) as NavHostFragment
        binding.appBarMain.bottomNavigation.setupWithNavController(navHostFragment.navController)

        addMenuProvider(MainActivityMenuProvider(this, settingStore))
    }

    override fun setToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        toggleNavigationDrawerDelegate.updateToolbar(toolbar)
    }


    /**
     * シンプルエディターの表示・非表示を行う
     */
    private fun ActivityMainBinding.setSimpleEditor() {
        SetSimpleEditor(
            supportFragmentManager,
            settingStore,
            appBarMain.fab
        ).invoke()
    }

    private val switchAccountButtonObserver = Observer<Int> {
        runOnUiThread {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(supportFragmentManager, "mainActivity")
        }
    }


    private val showFollowingsObserver = Observer<User.Id> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent = FollowFollowerActivity.newIntent(this, it, true)
        startActivity(intent)
    }

    private val showFollowersObserver = Observer<User.Id> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent = FollowFollowerActivity.newIntent(this, it, false)
        startActivity(intent)
    }

    @ExperimentalCoroutinesApi
    private val showProfileObserver = Observer<Account> {
        binding.drawerLayout.closeDrawerWhenOpened()
        val intent =
            UserDetailActivity.newInstance(this, userId = User.Id(it.accountId, it.remoteId))
        intent.putActivity(Activities.ACTIVITY_IN_APP)
        startActivity(intent)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initAccountViewModelListener() {
        mAccountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        mAccountViewModel.switchAccount.observe(this, switchAccountButtonObserver)
        mAccountViewModel.showFollowings.observe(this, showFollowingsObserver)
        mAccountViewModel.showFollowers.observe(this, showFollowersObserver)
        mAccountViewModel.showProfile.observe(this, showProfileObserver)
    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> {
                if (mBackPressedDelegate.back()) {
                    super.onBackPressed()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_again_to_finish),
                        Toast.LENGTH_SHORT
                    ).apply {
                        setGravity(Gravity.CENTER, 0, 0)
                        show()
                    }
                }
            }
        }
    }


    @MainThread
    private fun DrawerLayout.closeDrawerWhenOpened() {
        if (this.isDrawerOpen(GravityCompat.START)) {
            this.closeDrawer(GravityCompat.START)
        }
    }


    override fun onStart() {
        super.onStart()
        setBackgroundImage()
        applyUI()
    }

    private fun setBackgroundImage() {
        val path = settingStore.backgroundImagePath
        Glide.with(this)
            .load(path)
            .into(binding.appBarMain.contentMain.backgroundImage)
    }

    @MainThread
    private fun applyUI() {
        invalidateOptionsMenu()
        binding.setSimpleEditor()

        binding.appBarMain.bottomNavigation.visibility = if (settingStore.isClassicUI) {
            View.GONE
        } else {
            View.VISIBLE
        }

    }

    private fun showCreateNoteTaskStatusSnackBar(taskState: TaskState<Note>) {
        showNoteCreationResultSnackBar(taskState)
    }


    private fun onFabClicked() {
        when (val type = currentPageableTimelineViewModel.currentType.value.suitableType()) {
            is SuitableType.Other -> {
                startActivity(Intent(this, NoteEditorActivity::class.java))
            }
            is SuitableType.Gallery -> {
                val intent = Intent(this, GalleryPostsActivity::class.java)
                intent.action = Intent.ACTION_EDIT
                startActivity(intent)
            }
            is SuitableType.Channel -> {
                val accountId = accountStore.currentAccountId!!
                startActivity(
                    NoteEditorActivity.newBundle(
                        this,
                        channelId = Channel.Id(accountId, type.channelId)
                    )
                )
            }
        }
    }

    private fun showSendReportStateFrom(state: ReportState) {
        ReportStateHandler().invoke(binding.appBarMain.simpleNotification, state)
    }

    @Inject
    lateinit var misskeyAPIProvider: MisskeyAPIProvider

    @ExperimentalCoroutinesApi
    private fun getCurrentAccountMisskeyAPI(): Flow<MisskeyAPI?> {
        return accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            metaRepository.observe(it.instanceDomain)
        }.map {
            it?.let {
                misskeyAPIProvider.get(it.uri, it.getVersion())
            }
        }
    }

}


class MainNavigationImpl @Inject constructor(
    val activity: Activity
) : MainNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, MainActivity::class.java)
    }
}

