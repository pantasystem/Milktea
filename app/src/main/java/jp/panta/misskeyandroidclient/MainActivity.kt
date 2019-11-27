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
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.drive.DriveFragment
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
        //replaceTimelineFragment()
        init()


        val miApplication = application as MiApplication

        var init = false
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            if(!init){
                mNotesViewModel = ViewModelProvider(this, NotesViewModelFactory(it, miApplication)).get(NotesViewModel::class.java)

                mAccountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
                Log.d("MainActivity", "NotesViewModelのコネクション情報: ${mNotesViewModel.connectionInstance}")

                init()

                //initViewModelListener()
                ActionNoteHandler(this, mNotesViewModel).initViewModelListener()
                initAccountViewModelListener()

                setHeaderProfile(mainBinding)

                init = true
                //observeTab()
                Log.d("MainActivity", "初期化処理")
            }

        })

        miApplication.isSuccessLoadConnectionInstance.observe(this, Observer {
            if(!it){
                startActivity(Intent(this, AuthActivity::class.java))
                //finish()
            }
        })

        bottom_navigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home ->{
                    setFragment("home")
                    true
                }
                R.id.navigation_search ->{
                    setFragment("search")
                    true
                }
                R.id.navigation_notification ->{
                    setFragment("notification")
                    true
                }
                R.id.navigation_message_list ->{
                    setFragment("message")
                    true
                }
                else -> false
            }


        }
        test()

    }

    private fun init(){
        val ci = (application as MiApplication).currentConnectionInstanceLiveData.value
        if(ci != null){
            setFragment("home")
            //setHeaderProfile(ci)
        }

    }


    /*private val replyTargetObserver = Observer<PlaneNoteViewData> {
        //Log.d("MainActivity", "reply clicked :$it")
        val intent = Intent(this, NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_REPLY_TO_NOTE_ID, it.toShowNote.id)
        startActivity(intent)
    }

    private val reNoteTargetObserver = Observer<PlaneNoteViewData>{
        Log.d("MainActivity", "renote clicked :$it")
        val dialog = RenoteBottomSheetDialog()
        dialog.show(supportFragmentManager, "timelineFragment")

    }
    private val shareTargetObserver = Observer<PlaneNoteViewData> {
        Log.d("MainActivity", "share clicked :$it")
        ShareBottomSheetDialog().show(supportFragmentManager, "MainActivity")
    }
    private val targetUserObserver = Observer<User>{
        Log.d("MainActivity", "user clicked :$it")
    }

    private val statusMessageObserver = Observer<String>{
        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
    }

    private val quoteRenoteTargetObserver = Observer<PlaneNoteViewData>{
        val intent = Intent(this, NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_QUOTE_TO_NOTE_ID, it.toShowNote.id)
        startActivity(intent)
    }

    private val reactionTargetObserver = Observer<PlaneNoteViewData>{
        Log.d("MainActivity", "リアクションの対象ノートを選択:${it.toShowNote}")
        ReactionSelectionDialog().show(supportFragmentManager, "MainActivity")
    }

    private val noteTargetObserver = Observer<PlaneNoteViewData>{
        val intent = Intent(this, NoteDetailActivity::class.java)
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, it.toShowNote.id)
        startActivity(intent)
    }

    private fun initViewModelListener(){
        mNotesViewModel.replyTarget.removeObserver(replyTargetObserver)
        mNotesViewModel.replyTarget.observe(this, replyTargetObserver)

        mNotesViewModel.reNoteTarget.removeObserver(reNoteTargetObserver)
        mNotesViewModel.reNoteTarget.observe(this, reNoteTargetObserver)

        mNotesViewModel.shareTarget.removeObserver(shareTargetObserver)
        mNotesViewModel.shareTarget.observe(this, shareTargetObserver)

        mNotesViewModel.targetUser.removeObserver(targetUserObserver)
        mNotesViewModel.targetUser.observe(this, targetUserObserver)

        mNotesViewModel.statusMessage.removeObserver(statusMessageObserver)
        mNotesViewModel.statusMessage.observe(this, statusMessageObserver)

        mNotesViewModel.quoteRenoteTarget.removeObserver(quoteRenoteTargetObserver)
        mNotesViewModel.quoteRenoteTarget.observe(this, quoteRenoteTargetObserver)

        mNotesViewModel.reactionTarget.removeObserver(reactionTargetObserver)
        mNotesViewModel.reactionTarget.observe(this, reactionTargetObserver)

        mNotesViewModel.targetNote.removeObserver(noteTargetObserver)
        mNotesViewModel.targetNote.observe(this, noteTargetObserver)
    }*/

    private val switchAccountButtonObserver = Observer<Int>{
        runOnUiThread{
            drawer_layout.closeDrawer(GravityCompat.START)
            val dialog = AccountSwitchingDialog()
            dialog.show(supportFragmentManager, "mainActivity")
        }
    }

    private val switchAccountObserver = Observer<ConnectionInstance>{
        (application as MiApplication).switchAccount(it)
    }

    private fun initAccountViewModelListener(){
        mAccountViewModel.switchAccount.removeObserver(switchAccountButtonObserver)
        mAccountViewModel.switchAccount.observe(this, switchAccountButtonObserver)

        mAccountViewModel.switchTargetConnectionInstance.removeObserver(switchAccountObserver)
        mAccountViewModel.switchTargetConnectionInstance.observe(this, switchAccountObserver)
    }
    fun changeTitle(title: String?){
        toolbar.title = title
    }

    private fun test(){
        //startActivity(Intent(this, AuthActivity::class.java))
    }

    //default "home"
    private var currentFragmentTag = "home"
    private fun setFragment(tag: String){
        setBottomNavigationSelectState(tag)
        setTitleByTag(tag)

        val ft = supportFragmentManager.beginTransaction()

        val targetFragment = supportFragmentManager.findFragmentByTag(tag)
        val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)


        //表示しようとしているFragmentが表示(add)したことがない場合
        if(targetFragment == null){
            //supportFragmentManager.
            if(currentFragment != null){
                //currentをhideする
                ft.hide(currentFragment)
            }
            ft.add(R.id.content_main, newFragmentByTag(tag), tag)
            currentFragmentTag = tag
            ft.commit()
            return
        }

        //表示しているFragmentと表示しようとしているFragmentが同じ場合
        if(currentFragmentTag == tag && currentFragment != null){
            ft.commit()
            return
        }

        //表示しているFragmentと表示しようとしているFragmentが別でさらに既に存在している場合
        if(currentFragmentTag != tag && currentFragment != null){
            ft.hide(currentFragment)
            ft.show(targetFragment)
            currentFragmentTag = tag
            ft.commit()
            return
        }

    }

    private fun setBottomNavigationSelectState(tag: String){
        when(tag){
            "home" -> bottom_navigation.menu.findItem(R.id.navigation_home).isChecked = true
            "search" -> bottom_navigation.menu.findItem(R.id.navigation_search).isChecked = true
            "notification" -> bottom_navigation.menu.findItem(R.id.navigation_notification).isChecked = true
            "message" -> bottom_navigation.menu.findItem(R.id.navigation_message_list).isChecked = true
        }
    }

    private fun newFragmentByTag(tag: String): Fragment{
        return when(tag){
            "home" -> TabFragment()
            "search" -> SearchTopFragment()
            "drive" -> DriveFragment()
            "notification" -> NotificationFragment()
            "message" -> MessagingHistoryFragment()
            else -> throw IllegalArgumentException("サポートしていないタグです")
        }
    }

    private fun setTitleByTag(tag: String){
        when(tag){
            "home" -> changeTitle("Home")
            "search" -> changeTitle("Search")
            "drive" -> changeTitle("Drive")
            "notification" -> changeTitle("Notification")
            "message" -> changeTitle("Message")
        }
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
        }else if(currentFragmentTag != "home"){
            setFragment("home")
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
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
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }
}
