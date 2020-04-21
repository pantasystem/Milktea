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
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.users.PinNoteFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import java.lang.IllegalArgumentException

class UserDetailActivity : AppCompatActivity() {
    companion object{
        const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_ID"
        const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_NAME"
        const val EXTRA_IS_MAIN_ACTIVE = "jp.panta.misskeyandroidclient.EXTRA_IS_MAIN_ACTIVE"
    }

    private var mViewModel: UserDetailViewModel? = null

    private var mAccountRelation: AccountRelation? = null

    private var mUserId: String? = null
    private var mIsMainActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(this, R.layout.activity_user_detail)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val userId: String? = intent.getStringExtra(EXTRA_USER_ID)
        mUserId = userId
        val userName = intent.data?.getQueryParameter("userName")
            ?: intent.getStringExtra(EXTRA_USER_NAME)
        Log.d("UserDetailActivity", "userName:$userName")
        mIsMainActive = intent.getBooleanExtra(EXTRA_IS_MAIN_ACTIVE, true)

        val miApplication = applicationContext as MiApplication
        miApplication.currentAccount.observe(this, Observer {ar ->
            mAccountRelation = ar
            val viewModel = ViewModelProvider(this, UserDetailViewModelFactory(ar, miApplication, userId, userName))[UserDetailViewModel::class.java]
            mViewModel = viewModel
            binding.userViewModel = viewModel

            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ar, miApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel)
                .initViewModelListener()

            viewModel.load()
            viewModel.user.observe(this, Observer {
                val adapter =UserTimelinePagerAdapter(supportFragmentManager, ar.account, it.id)
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

            invalidateOptionsMenu()

        })





    }

    inner class UserTimelinePagerAdapter(
        fm: FragmentManager,
        val account: Account,
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

        val tab = menu?.findItem(R.id.nav_add_to_tab)
        val page = mAccountRelation?.pages?.firstOrNull {
            it.userId == mUserId
        }
        if(page == null){
            tab?.setIcon(R.drawable.ic_add_to_tab_24px)
        }else{
            tab?.setIcon(R.drawable.ic_remove_to_tab_24px)
        }

        menu?.let{
            setMenuTint(it)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                finishAndGoToMainActivity()
            }
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

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndGoToMainActivity()
    }

    private fun finishAndGoToMainActivity(){
        if(!mIsMainActive){
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }


    private fun addPageToTab(){
        val user = mViewModel?.user?.value
        user?: return

        val page = mAccountRelation?.pages?.firstOrNull {
            it.userId == mUserId && mUserId != null
        }
        val isAdded = page != null
        if(isAdded){
            (application as MiCore).removePageInCurrentAccount(page!!)
        }else{
            (application as MiApplication).addPageInCurrentAccount(NoteRequest.Setting(type = NoteType.USER, userId = user.id).apply{
                title = user.getDisplayUserName()
            })
        }

    }
}
