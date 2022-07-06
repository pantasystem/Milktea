package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.databinding.NavHeaderMainBinding
import jp.panta.misskeyandroidclient.ui.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.notes.view.ActionNoteHandler
import jp.panta.misskeyandroidclient.ui.notes.view.editor.SimpleEditorFragment
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notification.notificationMessageScope
import jp.panta.misskeyandroidclient.ui.settings.activities.PageSettingActivity
import jp.panta.misskeyandroidclient.ui.strings_helper.webSocketStateMessageScope
import jp.panta.misskeyandroidclient.ui.users.ReportStateHandler
import jp.panta.misskeyandroidclient.ui.users.viewmodel.ReportViewModel
import jp.panta.misskeyandroidclient.util.DoubleBackPressedFinishDelegate
import jp.panta.misskeyandroidclient.viewmodel.MainUiState
import jp.panta.misskeyandroidclient.viewmodel.MainViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.channel.ChannelActivity
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ui.SetTheme
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.SuitableType
import net.pantasystem.milktea.common_viewmodel.suitableType
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.drive.DriveActivity
import net.pantasystem.milktea.gallery.GalleryPostsActivity
import net.pantasystem.milktea.messaging.MessagingListActivity
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
    lateinit var setTheme: SetTheme


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
        setTheme.setTheme()
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

private class ShowNoteCreationResultSnackBar(
    private val activity: Activity,
    private val noteTaskExecutor: CreateNoteTaskExecutor,
    private val view: View
) {

    operator fun invoke(taskState: TaskState<Note>) {
        when (taskState) {
            is TaskState.Error -> {
                activity.getString(R.string.note_creation_failure).showSnackBar(
                    activity.getString(R.string.retry) to ({
                        noteTaskExecutor.dispatch(taskState.task)
                    })
                )
            }
            is TaskState.Success -> {
                activity.getString(R.string.successfully_created_note).showSnackBar(
                    activity.getString(R.string.show) to ({
                        activity.startActivity(
                            NoteDetailActivity.newIntent(activity, taskState.res.id)
                        )
                    })
                )
            }
            is TaskState.Executing -> {
            }
        }
    }

    private fun String.showSnackBar(action: Pair<String, (View) -> Unit>? = null) {
        val snackBar =
            Snackbar.make(view, this, Snackbar.LENGTH_LONG)
        if (action != null) {
            snackBar.setAction(action.first, action.second)
        }
        snackBar.show()
    }


}

private class ChangeNavMenuVisibilityFromAPIVersion(
    private val navView: NavigationView
) {
    operator fun invoke(api: MisskeyAPI) {
        navView.menu.also { menu ->
            menu.findItem(R.id.nav_antenna).isVisible = api is MisskeyAPIV12
            menu.findItem(R.id.nav_channel).isVisible = api is MisskeyAPIV12
            menu.findItem(R.id.nav_gallery).isVisible = api is MisskeyAPIV1275
        }
    }
}

private class ShowBottomNavigationBadgeDelegate(
    private val bottomNavigationView: BottomNavigationView
) {
    operator fun invoke(state: MainUiState) {
        if (state.unreadNotificationCount <= 0) {
            bottomNavigationView.getBadge(R.id.navigation_notification)
                ?.clearNumber()
        }
        if (state.unreadMessagesCount <= 0) {
            bottomNavigationView.getBadge(R.id.navigation_message_list)?.clearNumber()
        }
        bottomNavigationView.getOrCreateBadge(R.id.navigation_notification)
            .apply {
                isVisible = state.unreadNotificationCount > 0
                number = state.unreadNotificationCount
            }
        bottomNavigationView.getOrCreateBadge(R.id.navigation_message_list).apply {
            isVisible = state.unreadMessagesCount > 0
            number = state.unreadMessagesCount
        }
    }
}

private class ToggleNavigationDrawerDelegate(
    private val activity: Activity,
    private val drawerLayout: DrawerLayout
) {
    private var toggle: ActionBarDrawerToggle? = null

    @MainThread
    fun updateToolbar(toolbar: Toolbar) {
        if (toggle != null) {
            drawerLayout.removeDrawerListener(toggle!!)
        }
        toggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle!!.syncState()
    }

}

class MainNavigationImpl @Inject constructor(
    val activity: Activity
) : MainNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, MainActivity::class.java)
    }
}

interface ToolbarSetter {
    fun setToolbar(toolbar: Toolbar)
    fun setTitle(resId: Int)
}


private class StartActivityFromNavDrawerItems(
    val mainActivity: MainActivity
) {
    fun showNavDrawersActivityBy(item: MenuItem) {
        val activity = when (item.itemId) {
            R.id.nav_setting -> SettingsActivity::class.java
            R.id.nav_drive -> DriveActivity::class.java
            R.id.nav_favorite -> FavoriteActivity::class.java
            R.id.nav_list -> ListListActivity::class.java
            R.id.nav_antenna -> AntennaListActivity::class.java
            R.id.nav_draft -> DraftNotesActivity::class.java
            R.id.nav_gallery -> GalleryPostsActivity::class.java
            R.id.nav_channel -> ChannelActivity::class.java
            else -> throw IllegalStateException("未定義なNavigation Itemです")
        }
        mainActivity.startActivity(Intent(mainActivity, activity))
    }
}

private class MainActivityMenuProvider(
    val activity: MainActivity,
    val settingStore: SettingStore
) : MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main, menu)

        listOf(
            menu.findItem(R.id.action_messaging),
            menu.findItem(R.id.action_notification),
            menu.findItem(R.id.action_search)
        ).forEach {
            it.isVisible = settingStore.isClassicUI
        }

    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val idAndActivityMap = mapOf(
            R.id.action_settings to SettingsActivity::class.java,
            R.id.action_tab_setting to PageSettingActivity::class.java,
            R.id.action_notification to NotificationsActivity::class.java,
            R.id.action_messaging to MessagingListActivity::class.java,
            R.id.action_search to SearchActivity::class.java
        )

        val targetActivity = idAndActivityMap[menuItem.itemId]
            ?: return true
        activity.startActivity(Intent(activity, targetActivity))
        return true
    }
}

private class SetUpNavHeader(
    private val navView: NavigationView,
    private val lifecycleOwner: LifecycleOwner,
    private val accountViewModel: AccountViewModel
) {

    operator fun invoke() {
        DataBindingUtil.bind<NavHeaderMainBinding>(this.navView.getHeaderView(0))
        val headerBinding =
            DataBindingUtil.getBinding<NavHeaderMainBinding>(this.navView.getHeaderView(0))
        headerBinding?.lifecycleOwner = lifecycleOwner
        headerBinding?.accountViewModel = accountViewModel
    }
}

private class SetSimpleEditor(
    val fragmentManager: FragmentManager,
    val settingStore: SettingStore,
    val fab: FloatingActionButton,
) {
    operator fun invoke() {
        val ft = fragmentManager.beginTransaction()

        val editor = fragmentManager.findFragmentByTag("simpleEditor")

        if (settingStore.isSimpleEditorEnabled) {
            fab.visibility = View.GONE
            if (editor == null) {
                ft.replace(R.id.simpleEditorBase, SimpleEditorFragment(), "simpleEditor")
            }
        } else {
            fab.visibility = View.VISIBLE

            editor?.let {
                ft.remove(it)
            }

        }
        ft.commit()
    }
}