package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.databinding.NavHeaderMainBinding
import jp.panta.misskeyandroidclient.model.TaskState
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.model.streaming.stateEvent
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.util.BottomNavigationAdapter
import jp.panta.misskeyandroidclient.util.DoubleBackPressedFinishDelegate
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.messaging.MessagingHistoryFragment
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TabFragment
import jp.panta.misskeyandroidclient.view.notes.editor.SimpleEditorFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationMentionFragment
import jp.panta.misskeyandroidclient.view.notification.notificationMessageScope
import jp.panta.misskeyandroidclient.view.search.SearchTopFragment
import jp.panta.misskeyandroidclient.view.settings.activities.PageSettingActivity
import jp.panta.misskeyandroidclient.view.strings_helper.webSocketStateMessageScope
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mNotesViewModel: NotesViewModel
    @ExperimentalCoroutinesApi
    private lateinit var mAccountViewModel: AccountViewModel

    private var mBottomNavigationAdapter: MainBottomNavigationAdapter? = null


    private var fcmService: FCMService? = null

    private var mSettingStore: SettingStore? = null

    private val mBackPressedDelegate = DoubleBackPressedFinishDelegate()

    private var logger: Logger? = null

    private val mBinding: ActivityMainBinding by dataBinding()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(R.style.AppThemeDark)
        setTheme()
        setContentView(R.layout.activity_main)

        setSupportActionBar(mBinding.appBarMain.toolbar)

        val toggle = ActionBarDrawerToggle(
            this, mBinding.drawerLayout, mBinding.appBarMain.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        mBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        mBinding.navView.setNavigationItemSelectedListener(this)

        mBinding.appBarMain.fab.setOnClickListener{
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        val miApplication = application as MiApplication
        logger = miApplication.loggerFactory.create("MainActivity")

        mAccountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication))[AccountViewModel::class.java]
        initAccountViewModelListener()
        setHeaderProfile(mBinding)



        mNotesViewModel = ViewModelProvider(this, NotesViewModelFactory(miApplication)).get(NotesViewModel::class.java)
        ActionNoteHandler(this, mNotesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java]).initViewModelListener()

        // NOTE: メッセージの既読数をバッジに表示する
        miApplication.getCurrentAccount().filterNotNull().flatMapLatest {
            miApplication.getUnreadMessages().findByAccountId(it.accountId)
        }.map {
            it.size
        }.flowOn(Dispatchers.IO).onEach { count ->
            mBinding.appBarMain.bottomNavigation.getOrCreateBadge(R.id.navigation_message_list).let{
                it.isVisible = count > 0
                it.number = count
            }
        }.catch { e ->
            logger?.error("メッセージ既読数取得エラー", e = e)
        }.launchIn(lifecycleScope)

        // NOTE: 各ばーしょんに合わせMenuを制御している
        miApplication.getCurrentAccount().filterNotNull().map {
            miApplication.getMisskeyAPI(it)
        }.onEach { api ->
            mBinding.navView.menu.also { menu ->
                menu.findItem(R.id.nav_antenna).isVisible = api is MisskeyAPIV12
                menu.findItem(R.id.nav_gallery).isVisible = api is MisskeyAPIV1275
            }
        }.launchIn(lifecycleScope)

        miApplication.connectionStatus.observe(this) { status ->
            when (status) {
                ConnectionStatus.SUCCESS -> Log.d("MainActivity", "成功")
                ConnectionStatus.ACCOUNT_ERROR -> {
                    startActivity(Intent(this, AuthorizationActivity::class.java))

                    finish()
                }
                ConnectionStatus.NETWORK_ERROR -> {
                    Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("MainActivity", "not initialized")
            }
        }

        // NOTE: 通知の既読数を表示する
        miApplication.getCurrentAccount().filterNotNull().flatMapLatest {
            miApplication.getNotificationRepository().countUnreadNotification(it.accountId)
        }.flowOn(Dispatchers.IO).onEach { count ->
            if(count <= 0) {
                mBinding.appBarMain.bottomNavigation.getBadge(R.id.navigation_notification)?.clearNumber()
            }
            mBinding.appBarMain.bottomNavigation.getOrCreateBadge(R.id.navigation_notification).apply{
                isVisible = count > 0
                number = count
            }
        }.catch { e ->
            logger?.error("通知既読数取得エラー", e = e)
        }.launchIn(lifecycleScope)

        // NOTE: 最新の通知をSnackBar等に表示する
        miApplication.getCurrentAccount().filterNotNull().flatMapLatest { ac ->
            miApplication.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map { body ->
                body as? ChannelBody.Main.Notification
            }.filterNotNull().map {
                ac to it
            }
        }.map {
            miApplication.getGetters().notificationRelationGetter.get(it.first, it.second.body)
        }.flowOn(Dispatchers.IO).onEach { notificationRelation ->
            notificationMessageScope {
                notificationRelation.showSnackBarMessage(mBinding.appBarMain.simpleNotification)
            }
        }.catch { e ->
            logger?.error("通知取得エラー", e = e)
        }.launchIn(lifecycleScope + Dispatchers.Main)

        if(BuildConfig.DEBUG) {
            lifecycleScope.launchWhenResumed {
                miApplication.getCurrentAccount().filterNotNull().flatMapLatest {
                    miApplication.getSocket(it).stateEvent()
                }.catch { e ->
                    logger?.error("WebSocket　状態取得エラー", e)
                }.collect {
                    webSocketStateMessageScope {
                        it.showToastMessage()
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            miApplication.getTaskExecutor().tasks.mapNotNull {
                it as? TaskState.Success<*>
            }.mapNotNull {
                it.res as? Note
            }.collect {
                showSnackBar(getString(R.string.successfully_created_note))
            }
        }

        startService(Intent(this, NotificationService::class.java))
        mBottomNavigationAdapter = MainBottomNavigationAdapter(savedInstanceState, mBinding.appBarMain.bottomNavigation)

    }


    inner class MainBottomNavigationAdapter(savedInstanceState: Bundle?, bottomNavigation: BottomNavigationView)
        : BottomNavigationAdapter(bottomNavigation, supportFragmentManager, R.id.navigation_home, R.id.content_main, savedInstanceState){

        var currentMenuItem: MenuItem? = null

        override fun viewChanged(menuItem: MenuItem, fragment: Fragment) {
            super.viewChanged(menuItem, fragment)
            when(menuItem.itemId){
                R.id.navigation_home -> changeTitle(getString(R.string.menu_home))
                R.id.navigation_search -> changeTitle(getString(R.string.search))
                R.id.navigation_notification -> changeTitle(getString(R.string.notification))
                R.id.navigation_message_list -> changeTitle(getString(R.string.message))
            }
            currentMenuItem = menuItem
        }
        override fun getItem(menuItem: MenuItem): Fragment? {
            return when(menuItem.itemId){
                R.id.navigation_home -> TabFragment()
                R.id.navigation_search -> SearchTopFragment()
                R.id.navigation_notification -> NotificationMentionFragment()
                R.id.navigation_message_list -> MessagingHistoryFragment()
                else -> null
            }
        }

        override fun menuRetouched(menuItem: MenuItem, fragment: Fragment) {
            if(fragment is ScrollableTop){
                fragment.showTop()
            }
        }


    }

    /**
     * シンプルエディターの表示・非表示を行う
     */
    private fun setSimpleEditor(){
        val miApplication = applicationContext as MiApplication
        val ft = supportFragmentManager.beginTransaction()

        val editor = supportFragmentManager.findFragmentByTag("simpleEditor")

        if(miApplication.getSettingStore().isSimpleEditorEnabled){
            mBinding.appBarMain.fab.visibility = View.GONE
            if(editor == null){
                ft.replace(R.id.simpleEditorBase, SimpleEditorFragment(), "simpleEditor")
            }
        }else{
            mBinding.appBarMain.fab.visibility = View.VISIBLE

            editor?.let{
                ft.remove(it)
            }

        }
        ft.commit()
    }


    private fun showSnackBar(message: String) {
        val snackBar = Snackbar.make(mBinding.appBarMain.simpleNotification, message, Snackbar.LENGTH_LONG)

        snackBar.show()
    }

    private val switchAccountButtonObserver = Observer<Int>{
        runOnUiThread{
            mBinding.drawerLayout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(supportFragmentManager, "mainActivity")
        }
    }


    private val showFollowingsObserver = Observer<User.Id>{
        closeDrawerWhenOpenedDrawer()
        val intent = FollowFollowerActivity.newIntent(this, it, true)
        startActivity(intent)
    }

    private val showFollowersObserver = Observer<User.Id>{
        closeDrawerWhenOpenedDrawer()
        val intent = FollowFollowerActivity.newIntent(this, it, false)
        startActivity(intent)
    }

    @ExperimentalCoroutinesApi
    private val showProfileObserver = Observer<Account>{
        closeDrawerWhenOpenedDrawer()
        val intent = UserDetailActivity.newInstance(this, userId = User.Id(it.accountId, it.remoteId))
        intent.putActivity(Activities.ACTIVITY_IN_APP)
        startActivity(intent)
    }
    @ExperimentalCoroutinesApi
    private fun initAccountViewModelListener(){
        mAccountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        mAccountViewModel.switchAccount.observe(this, switchAccountButtonObserver)

        mAccountViewModel.showFollowings.observe(this, showFollowingsObserver)
        mAccountViewModel.showFollowers.observe(this, showFollowersObserver)
        mAccountViewModel.showProfile.observe(this, showProfileObserver)
    }
    fun changeTitle(title: String?){
        supportActionBar?.title = title
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setHeaderProfile(activityMainBinding: ActivityMainBinding){

        DataBindingUtil.bind<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))
        val headerBinding = DataBindingUtil.getBinding<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))
        headerBinding?.lifecycleOwner = this
        mAccountViewModel
        headerBinding?.accountViewModel = mAccountViewModel

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            mBottomNavigationAdapter?.currentMenuItem?.itemId != R.id.navigation_home -> {
                mBottomNavigationAdapter?.setCurrentFragment(R.id.navigation_home)
            }
            else -> {
                if(mBackPressedDelegate.back()){
                    super.onBackPressed()
                }else{
                    Toast.makeText(this, getString(R.string.please_again_to_finish), Toast.LENGTH_SHORT).apply{
                        setGravity(Gravity.CENTER, 0, 0)
                        show()
                    }
                }
            }
        }
    }

    @MainThread
    private fun closeDrawerWhenOpenedDrawer(){
        if(mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val isClassicUI = getSettingStore().isClassicUI
        val targetItems = listOf(
            menu.findItem(R.id.action_messaging),
            menu.findItem(R.id.action_notification),
            menu.findItem(R.id.action_search)
        )

        targetItems.forEach{
            it.isVisible = isClassicUI
        }

        setMenuTint(menu)
        return true
    }

    private fun getSettingStore(): SettingStore{
        val store: SettingStore = mSettingStore ?: SettingStore(getSharedPreferences(getPreferenceName(), Context.MODE_PRIVATE))
        mSettingStore = store
        return store
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_tab_setting-> {
                startActivity(Intent(this,
                    PageSettingActivity::class.java))
                true
            }
            R.id.action_notification ->{
                startActivity(Intent(this, NotificationsActivity::class.java))
                true
            }
            R.id.action_messaging ->{
                startActivity(Intent(this, MessagingListActivity::class.java))
                true
            }
            R.id.action_search ->{
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

            R.id.nav_setting ->{
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_drive ->{
                startActivity(Intent(this, DriveActivity::class.java))
            }
            R.id.nav_favorite ->{
                startActivity(Intent(this, FavoriteActivity::class.java))
            }
            R.id.nav_list ->{
                startActivity(
                    Intent(this, ListListActivity::class.java)
                )
            }
            R.id.nav_antenna ->{
                startActivity(Intent(this, AntennaListActivity::class.java))
            }
            R.id.nav_draft ->{
                startActivity(
                    Intent(this, DraftNotesActivity::class.java)
                )
            }
            R.id.nav_gallery -> {
                startActivity(Intent(this, GalleryPostsActivity::class.java))
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }
    override fun onStart() {
        super.onStart()
        setBackgroundImage()
        applyUI()
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.d("MainActivity", "#onSaveInstanceStateが呼び出された")

        mBottomNavigationAdapter?.saveState(outState)
    }


    private fun setBackgroundImage(){
        val path = SettingStore(getSharedPreferences(getPreferenceName() ,Context.MODE_PRIVATE)).backgroundImagePath
        Glide.with(this)
            .load(path)
            .into(mBinding.appBarMain.contentMain.backgroundImage)
    }

    @MainThread
    private fun applyUI(){
        invalidateOptionsMenu()
        setSimpleEditor()

        mBinding.appBarMain.bottomNavigation.visibility = if(getSettingStore().isClassicUI){
            View.GONE
        }else{
            View.VISIBLE
        }
        if(getSettingStore().isClassicUI){
            mBottomNavigationAdapter?.setCurrentFragment(R.id.navigation_home)
        }
    }

}
