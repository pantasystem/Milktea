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
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.ui.main.*
import jp.panta.misskeyandroidclient.ui.main.viewmodel.MainViewModel
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModel
import net.pantasystem.milktea.common_android_ui.report.ReportViewModel
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.ScrollToTopViewModel
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.notes.draft.DraftNoteService
import net.pantasystem.milktea.note.renote.RenoteResultHandler
import net.pantasystem.milktea.note.renote.RenoteViewModel
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.worker.note.CreateNoteWorkerExecutor
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
    lateinit var createNoteWorkerExecutor: CreateNoteWorkerExecutor

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var setTheme: ApplyTheme

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var draftNoteService: DraftNoteService

    @Inject
    lateinit var featureEnables: FeatureEnables

    private val mainViewModel: MainViewModel by viewModels()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by viewModels()

    private val reportViewModel: ReportViewModel by viewModels()

    private val scrollToTopViewModel: ScrollToTopViewModel by viewModels()

    private val renoteViewMode by viewModels<RenoteViewModel>()

    private lateinit var toggleNavigationDrawerDelegate: ToggleNavigationDrawerDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme.invoke()
        setContentView(R.layout.activity_main)

        toggleNavigationDrawerDelegate = ToggleNavigationDrawerDelegate(this, binding.drawerLayout)

        binding.navView.setNavigationItemSelectedListener { item ->
            MainActivityNavigationDrawerMenuItemClickListener(this, mAccountViewModel)
                .onSelect(item)
            binding.drawerLayout.closeDrawerWhenOpened()
            false
        }

        binding.appBarMain.fab.setOnClickListener {
            onFabClicked()
        }

        binding.appBarMain.bottomNavigation.setOnItemReselectedListener {
            scrollToTopViewModel.scrollToTop()
        }

        AccountViewModelHandler(binding, this, mAccountViewModel).setup()
        SetUpNavHeader(binding.navView, this, mAccountViewModel).invoke()

        ActionNoteHandler(
            this,
            mNotesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore,
        ).initViewModelListener()


        setupNavigation()
        setupOnBackPressedDispatcherCallBack()

        addMenuProvider(MainActivityMenuProvider(this, settingStore))

        MainActivityEventHandler(
            activity = this,
            accountStore = accountStore,
            binding = binding,
            lifecycleOwner = this,
            lifecycleScope = lifecycleScope,
            mainViewModel = mainViewModel,
            createNoteWorkerExecutor = createNoteWorkerExecutor,
            reportViewModel = reportViewModel,
            requestPostNotificationsPermissionLauncher = requestPermissionLauncher,
            changeNavMenuVisibilityFromAPIVersion = ChangeNavMenuVisibilityFromAPIVersion(binding.navView, featureEnables),
            configStore = settingStore,
            draftNoteService = draftNoteService,
            currentPageableTimelineViewModel = currentPageableTimelineViewModel
        ).setup()

        RenoteResultHandler(
            viewModel = renoteViewMode,
            lifecycle = lifecycle,
            scope = lifecycleScope,
            context = this
        ).setup()

        if (savedInstanceState == null) {
            handleIntent()
        }

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    override fun setToolbar(toolbar: Toolbar, visibleTitle: Boolean) {
        setSupportActionBar(toolbar)
        toggleNavigationDrawerDelegate.updateToolbar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(visibleTitle)
    }

    override fun onResume() {
        super.onResume()
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
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

    private fun handleIntent() {
        MainActivityInitialIntentHandler(
            binding.appBarMain.bottomNavigation,
            this,
            userDetailNavigation,
            accountStore.accountRepository
        ).invoke(intent)
    }

    private fun setupOnBackPressedDispatcherCallBack() {
        SetupOnBackPressedDispatcherHandler(
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

