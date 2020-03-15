package jp.panta.misskeyandroidclient

import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.databinding.NavHeaderMainBinding
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.BottomNavigationAdapter
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.messaging.MessagingHistoryFragment
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TabFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import jp.panta.misskeyandroidclient.view.search.SearchTopFragment
import jp.panta.misskeyandroidclient.view.settings.activities.TabSettingActivity
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var mNotesViewModel: NotesViewModel
    private lateinit var mAccountViewModel: AccountViewModel

    private var mBottomNavigationAdapter: MainBottomNavigationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(R.style.AppThemeDark)
        setTheme()

        setContentView(R.layout.activity_main)

        val mainBinding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        fab.setOnClickListener{
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }


        val miApplication = application as MiApplication

        mAccountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication.connectionInstanceDao!!))[AccountViewModel::class.java]
        initAccountViewModelListener()
        setHeaderProfile(mainBinding)

        var init = false
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            if(!init){
                mNotesViewModel = ViewModelProvider(this, NotesViewModelFactory(it, miApplication)).get(NotesViewModel::class.java)

                Log.d("MainActivity", "NotesViewModelのコネクション情報: ${mNotesViewModel.connectionInstance}")
                ActionNoteHandler(this, mNotesViewModel).initViewModelListener()
                init = true
                Log.d("MainActivity", "初期化処理")
            }

        })



        miApplication.isSuccessCurrentAccount.observe(this, Observer {
            if(!it){
                startActivity(Intent(this, AuthActivity::class.java))
            }
        })


        startService(Intent(this, NotificationService::class.java))
        mBottomNavigationAdapter = MainBottomNavigationAdapter()

    }



    inner class MainBottomNavigationAdapter
        : BottomNavigationAdapter(bottom_navigation, supportFragmentManager, R.id.navigation_home, R.id.content_main){
        private val home = bottom_navigation.menu.findItem(R.id.navigation_home)
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
                R.id.navigation_notification -> NotificationFragment()
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

    private val switchAccountButtonObserver = Observer<Int>{
        runOnUiThread{
            drawer_layout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(supportFragmentManager, "mainActivity")
        }
    }

    private val switchAccountObserver = Observer<ConnectionInstance>{
        (application as MiApplication).switchCurrentAccount(it)
    }

    private val showFollowingsObserver = Observer<Unit>{
        closeDrawerWhenOpenedDrawer()
        val intent = Intent(this, FollowFollowerActivity::class.java).apply{
            putExtra(FollowFollowerActivity.EXTRA_VIEW_CURRENT, FollowFollowerActivity.FOLLOWING_VIEW_MODE)
        }
        startActivity(intent)
    }

    private val showFollowersObserver = Observer<Unit>{
        closeDrawerWhenOpenedDrawer()
        val intent = Intent(this, FollowFollowerActivity::class.java).apply {
            putExtra(FollowFollowerActivity.EXTRA_VIEW_CURRENT, FollowFollowerActivity.FOLLOWER_VIEW_MODE)
        }
        startActivity(intent)
    }

    private val showProfileObserver = Observer<User>{
        closeDrawerWhenOpenedDrawer()
        val intent = Intent(this, UserDetailActivity::class.java)
        intent.putExtra(UserDetailActivity.EXTRA_USER_ID, it.id)
        startActivity(intent)
    }
    private fun initAccountViewModelListener(){
        mAccountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        mAccountViewModel.switchAccount.observe(this, switchAccountButtonObserver)

        mAccountViewModel.switchTargetConnectionInstance.removeObserver(switchAccountObserver)
        mAccountViewModel.switchTargetConnectionInstance.observe(this, switchAccountObserver)

        mAccountViewModel.showFollowings.observe(this, showFollowingsObserver)
        mAccountViewModel.showFollowers.observe(this, showFollowersObserver)
        mAccountViewModel.showProfile.observe(this, showProfileObserver)
    }
    fun changeTitle(title: String?){
        toolbar.title = title
    }

    private fun setHeaderProfile(activityMainBinding: ActivityMainBinding){


        DataBindingUtil.bind<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))
        val headerBinding = DataBindingUtil.getBinding<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))
        headerBinding?.accountViewModel = mAccountViewModel

        (application as MiApplication).currentAccountLiveData.observe(this, Observer {
            headerBinding?.user = it
        })
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }else if(mBottomNavigationAdapter?.currentMenuItem?.itemId != R.id.navigation_home){
            mBottomNavigationAdapter?.setCurrentFragment(R.id.navigation_home)
        }else{
            super.onBackPressed()
        }
    }

    private fun closeDrawerWhenOpenedDrawer(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        setMenuTint(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_tab_setting-> {
                startActivity(Intent(this,
                    TabSettingActivity::class.java))
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
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }


}
