package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.main.*
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.report.ReportViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.common_viewmodel.viewmodel.AccountViewModel
import net.pantasystem.milktea.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolbarSetter {

    private val mNotesViewModel: NotesViewModel by viewModels()

    private val mAccountViewModel: AccountViewModel by viewModels()

    private val binding: ActivityMainBinding by dataBinding()

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var noteTaskExecutor: CreateNoteTaskExecutor

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var setTheme: ApplyTheme


    private val mainViewModel: MainViewModel by viewModels()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by viewModels()

    private val reportViewModel: ReportViewModel by viewModels()

    private lateinit var toggleNavigationDrawerDelegate: ToggleNavigationDrawerDelegate

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        setContentView(R.layout.activity_main)

        toggleNavigationDrawerDelegate = ToggleNavigationDrawerDelegate(this, binding.drawerLayout)

        binding.navView.setNavigationItemSelectedListener { item ->
            StartActivityFromNavDrawerItems(this).showNavDrawersActivityBy(item)
            binding.drawerLayout.closeDrawerWhenOpened()
            false
        }

        binding.appBarMain.fab.setOnClickListener {
            onFabClicked()
        }

        AccountViewModelHandler(binding, this, mAccountViewModel).setup()
        SetUpNavHeader(binding.navView, this, mAccountViewModel).invoke()

        ActionNoteHandler(
            this,
            mNotesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore,
        ).initViewModelListener()


        startService(Intent(this, NotificationService::class.java))

        setupNavigation()
        setupOnBackPressedDispatcherCallBack()

        addMenuProvider(MainActivityMenuProvider(this, settingStore))

        MainActivityEventCollector(
            activity = this,
            accountStore = accountStore,
            authorizationNavigation = authorizationNavigation,
            binding = binding,
            lifecycleOwner = this,
            lifecycleScope = lifecycleScope,
            mainViewModel = mainViewModel,
            noteTaskExecutor = noteTaskExecutor,
            reportViewModel = reportViewModel,
            requestPostNotificationsPermissionLauncher = requestPermissionLauncher,
            changeNavMenuVisibilityFromAPIVersion = ChangeNavMenuVisibilityFromAPIVersion(binding.navView),
        ).setup()


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

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.contentMain) as NavHostFragment
        val navController = navHostFragment.navController
        binding.appBarMain.bottomNavigation.setupWithNavController(navController)
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, destination.label.toString())
                param(FirebaseAnalytics.Param.SCREEN_CLASS, destination.label.toString())
            }
        }
    }

    private fun setupOnBackPressedDispatcherCallBack() {
        SetupOnBackPressedDispatcherCallback(
            this,
            binding
        ).setup()
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

    private fun onFabClicked() {
        FabClickHandler(currentPageableTimelineViewModel, this, accountStore).onClicked()
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        mainViewModel.onPushNotificationConfirmed()
    }
}


class MainNavigationImpl @Inject constructor(
    val activity: Activity
) : MainNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, MainActivity::class.java)
    }
}

