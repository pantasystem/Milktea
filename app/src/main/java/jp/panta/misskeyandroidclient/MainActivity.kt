package jp.panta.misskeyandroidclient

import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import jp.panta.misskeyandroidclient.databinding.ActivityMainBinding
import jp.panta.misskeyandroidclient.databinding.NavHeaderMainBinding
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.message.MessageListFragment
import jp.panta.misskeyandroidclient.view.notes.RenoteBottomSheetDialog
import jp.panta.misskeyandroidclient.view.notes.TabFragment
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import jp.panta.misskeyandroidclient.view.search.SearchFragment
import jp.panta.misskeyandroidclient.viewmodel.main.MainViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mNotesViewModel: NotesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(R.style.AppThemeDark)

        setContentView(R.layout.activity_main)

        val mainBinding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /*val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        //replaceTimelineFragment()
        init()

        /*val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainViewModel.test.observe(this, Observer {
            Log.d("MainActivity", "値の更新があった更新内容: $it")
        })*/

        val miApplication = application as MiApplication

        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            init()
            setHeaderProfile(it, mainBinding)
            mNotesViewModel = ViewModelProvider(this, NotesViewModelFactory(it, miApplication)).get(NotesViewModel::class.java)
            initViewModelListener()
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

    private fun initViewModelListener(){
        mNotesViewModel.replyTarget.observe(this, Observer{
            Log.d("MainActivity", "reply clicked :$it")
        })

        mNotesViewModel.reNoteTarget.observe(this, Observer{
            Log.d("MainActivity", "renote clicked :$it")
            val dialog = RenoteBottomSheetDialog()
            dialog.show(supportFragmentManager, "timelineFragment")

        })

        mNotesViewModel.shareTarget.observe(this, Observer{
            Log.d("MainActivity", "share clicked :$it")
        })

        mNotesViewModel.targetUser.observe(this, Observer{
            Log.d("MainActivity", "user clicked :$it")
        })

        mNotesViewModel.statusMessage.observe(this, Observer{
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        mNotesViewModel.quoteRenoteTarget.observe(this, Observer{
            startActivity(Intent(this, NoteEditorActivity::class.java))
        })

        mNotesViewModel.reactionTarget.observe(this, Observer{
            Log.d("MainActivity", "リアクションの対象ノートを選択:${it.toShowNote}")
        })
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
            "search" -> SearchFragment()
            "notification" -> NotificationFragment()
            "message" -> MessageListFragment()
            else -> throw IllegalArgumentException("サポートしていないタグです")
        }
    }

    private fun setHeaderProfile(ci: ConnectionInstance, activityMainBinding: ActivityMainBinding){


        DataBindingUtil.bind<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))
        val headerBinding = DataBindingUtil.getBinding<NavHeaderMainBinding>(activityMainBinding.navView.getHeaderView(0))

        runOnUiThread {
            //nav_view.name.text = "namenamename"

        }
        val i = ci.getI()
        if(i != null){
            (application as MiApplication).misskeyAPIService?.i(I(i))?.enqueue( object: Callback<User>{
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    Log.d("MainActivity", "i: ${response.body()}")
                    //binding.user = response.body()
                    headerBinding?.user = response.body()
                }

                override fun onFailure(call: Call<User>, t: Throwable) {

                }
            })
        }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_tools -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
