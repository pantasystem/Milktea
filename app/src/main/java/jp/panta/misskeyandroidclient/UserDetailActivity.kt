package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityUserDetailBinding
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.view.users.PinNoteFragment
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import kotlinx.android.synthetic.main.activity_user_detail.*
import java.lang.IllegalArgumentException

class UserDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_ID"
    }

    private var mViewModel: UserDetailViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(this, R.layout.activity_user_detail)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val userId: String? = intent.getStringExtra(EXTRA_USER_ID)
        val userName = intent.data?.getQueryParameter("userName")
        Log.d("UserDetailActivity", "userName:$userName")

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {ci ->
            val viewModel = ViewModelProvider(this, UserDetailViewModelFactory(ci, miApplication, userId, userName))[UserDetailViewModel::class.java]
            mViewModel = viewModel
            binding.userViewModel = viewModel

            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ci, miApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel)
                .initViewModelListener()

            viewModel.load()
            viewModel.user.observe(this, Observer {
                val adapter =UserTimelinePagerAdapter(supportFragmentManager, ci, it.id)
                //userTimelinePager.adapter = adapter
                binding.userTimelinePager.adapter = adapter
                binding.userTimelineTab.setupWithViewPager(binding.userTimelinePager)
                supportActionBar?.title = it.getDisplayUserName()
            })


            viewModel.userName.observe(this, Observer{
                supportActionBar?.title = it
            })
            //userTimelineTab.setupWithViewPager()
            viewModel.showFollowers.observe(this, Observer {
                val intent = Intent(this, FollowFollowerActivity::class.java)
                intent.putExtra(FollowFollowerActivity.EXTRA_USER, it)
                intent.putExtra(FollowFollowerActivity.EXTRA_VIEW_CURRENT, FollowFollowerActivity.FOLLOWER_VIEW_MODE)
                startActivity(intent)
            })

            viewModel.showFollows.observe(this, Observer{
                val intent = Intent(this, FollowFollowerActivity::class.java)
                intent.putExtra(FollowFollowerActivity.EXTRA_USER, it)
                intent.putExtra(FollowFollowerActivity.EXTRA_VIEW_CURRENT, FollowFollowerActivity.FOLLOWING_VIEW_MODE)
                startActivity(intent)
            })

            val updateMenu = Observer<Boolean> {
                invalidateOptionsMenu()
            }
            viewModel.isBlocking.observe(this, updateMenu)
            viewModel.isMuted.observe(this, updateMenu)

        })





    }

    inner class UserTimelinePagerAdapter(
        fm: FragmentManager,
        val connectionInstance: ConnectionInstance,
        val userId: String
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val titles = listOf(getString(R.string.post), getString(R.string.pin), getString(R.string.media))
        private val requestMedia = NoteRequest.Setting(
            NoteType.USER,
            withFiles = true,
            userId = userId
        )
        private val requestTimeline = NoteRequest.Setting(
            NoteType.USER,
            userId = userId
        )
        override fun getCount(): Int {
            return titles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> TimelineFragment.newInstance(requestTimeline)
                1 -> PinNoteFragment()
                2 -> TimelineFragment.newInstance(requestMedia)
                else -> throw IllegalArgumentException("こんなものはない！！")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_user_menu, menu)
        if(menu != null){
            setMenuTint(menu)
        }
        val block = menu?.findItem(R.id.block)
        val mute = menu?.findItem(R.id.mute)
        val unblock = menu?.findItem(R.id.unblock)
        val unmute = menu?.findItem(R.id.unmute)
        mute?.isVisible = !(mViewModel?.isMuted?.value?: true)
        block?.isVisible = !(mViewModel?.isBlocking?.value?: true)
        unblock?.isVisible = mViewModel?.isBlocking?.value?: false
        unmute?.isVisible = mViewModel?.isMuted?.value?: false
        if(mViewModel?.isMine == true){
            block?.isVisible = false
            mute?.isVisible = false
            unblock?.isVisible = false
            unmute?. isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish()
            R.id.block ->{
                mViewModel?.block()
            }
            R.id.mute ->{
                mViewModel?.mute()
            }
            R.id.unblock ->{
                mViewModel?.unblock()
            }
            R.id.unmute ->{
                mViewModel?.unmute()
            }
            R.id.nav_add_to_tab ->{
                addPageToTab()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun addPageToTab(){
        val user = mViewModel?.user?.value
        if(user != null){
            (application as MiApplication).addPageToNoteSettings(NoteRequest.Setting(type = NoteType.USER, userId = user.id).apply{
                title = user.getDisplayUserName()
            })
        }
    }
}
