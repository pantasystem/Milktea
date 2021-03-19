package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.TaskStackBuilder
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import jp.panta.misskeyandroidclient.databinding.ActivityUserDetailBinding
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.users.PinNoteFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserDetailActivity : AppCompatActivity() {
    companion object{
        private const val EXTRA_USER_ID = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_ID"
        private const val EXTRA_USER_NAME = "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_NAME"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroiclient.UserDetailActivity.EXTRA_ACCOUNT_ID"
        const val EXTRA_IS_MAIN_ACTIVE = "jp.panta.misskeyandroidclient.EXTRA_IS_MAIN_ACTIVE"

        fun newInstance(context: Context, userName: String? = null, userId: User.Id? = null): Intent {
            return Intent(context, UserDetailActivity::class.java).apply {
                userName?.let{
                    putExtra(EXTRA_USER_NAME, userName)
                }
                userId?.let{
                    putExtra(EXTRA_USER_ID, userId.id)
                    putExtra(EXTRA_ACCOUNT_ID, userId.accountId)
                }
            }
        }
    }

    private var mViewModel: UserDetailViewModel? = null

    private var mAccountRelation: Account? = null

    private var mUserId: User.Id? = null
    private var mIsMainActive: Boolean = true

    private var mParentActivity: Activities? = null

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(this, R.layout.activity_user_detail)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mParentActivity = intent.getParentActivity()

        val userId: User.Id? = intent.getSerializableExtra(EXTRA_USER_ID) as? User.Id
        mUserId = userId
        val userName = intent.data?.getQueryParameter("userName")
            ?: intent.getStringExtra(EXTRA_USER_NAME)
            ?: intent.data?.path?.let{ path ->
                if(path.startsWith("/")){
                    path.substring(1, path.length)
                }else{
                    path
                }
            }
        Log.d("UserDetailActivity", "userName:$userName")
        mIsMainActive = intent.getBooleanExtra(EXTRA_IS_MAIN_ACTIVE, true)

        val miApplication = applicationContext as MiApplication

        val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(miApplication))[NotesViewModel::class.java]
        ActionNoteHandler(this, notesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java])
            .initViewModelListener()

        miApplication.getCurrentAccount().filterNotNull().onEach { ar ->
            mAccountRelation = ar
            val viewModel = ViewModelProvider(this, UserDetailViewModelFactory(miApplication, userId, userName))[UserDetailViewModel::class.java]
            mViewModel = viewModel
            binding.userViewModel = viewModel



            viewModel.load()
            viewModel.user.observe(this,  {
                val adapter =UserTimelinePagerAdapter(supportFragmentManager, ar, it.id.id)
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
                it?.let{
                    val intent = FollowFollowerActivity.newIntent(this, it.id, isFollowing = false)
                    startActivity(intent)
                }
            })

            viewModel.showFollows.observe(this, {
                it?.let{
                    val intent = FollowFollowerActivity.newIntent(this, it.id, true)
                    startActivity(intent)
                }
            })

            val updateMenu = Observer<Boolean> {
                invalidateOptionsMenu()
            }
            viewModel.isBlocking.observe(this, updateMenu)
            viewModel.isMuted.observe(this, updateMenu)

            invalidateOptionsMenu()


            binding.showRemoteUser.setOnClickListener {
                viewModel.user.value?.url?.let{
                    val uri = Uri.parse(it)
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                }
            }

        }.launchIn(lifecycleScope)






    }

    inner class UserTimelinePagerAdapter(
        fm: FragmentManager,
        val account: Account,
        val userId: String
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val titles = listOf(getString(R.string.post), getString(R.string.pin), getString(R.string.media))
        private val requestMedia = Pageable.UserTimeline(userId, withFiles = true)

        private val requestTimeline = Pageable.UserTimeline(userId)
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

    @ExperimentalCoroutinesApi
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
        if(mViewModel?.isMine?.value == true){
            block?.isVisible = false
            mute?.isVisible = false
            unblock?.isVisible = false
            unmute?. isVisible = false
        }

        val tab = menu?.findItem(R.id.nav_add_to_tab)
        val page = mAccountRelation?.pages?.firstOrNull {
            val pageable = it.pageable()
            if(pageable is Pageable.UserTimeline){
                pageable.userId == mUserId?.id
            }else{
                false
            }
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

    @ExperimentalCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("UserDetail", "mParentActivity: $mParentActivity")

        when(item.itemId){
            android.R.id.home -> {
                finishAndGoToMainActivity()
                return true
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
            R.id.add_list ->{
                val intent = Intent(this, ListListActivity::class.java)
                intent.putExtra(ListListActivity.EXTRA_ADD_USER_ID, mUserId)
                startActivity(intent)
            }
            else -> return false

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun finishAndGoToMainActivity(){
        if(mParentActivity == null || mParentActivity == Activities.ACTIVITY_OUT_APP) {
            val upIntent = Intent(this, MainActivity::class.java)
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            if(shouldUpRecreateTask(upIntent)){
                TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
                finish()
            }else{
                navigateUpTo(upIntent)
            }


            return
        }
        finish()
    }


    @ExperimentalCoroutinesApi
    private fun addPageToTab(){
        val user = mViewModel?.user?.value
        user?: return

        val page = mAccountRelation?.pages?.firstOrNull {
            val pageable = it.pageable()
            if(pageable is Pageable.UserTimeline){
                pageable.userId == mUserId?.id && mUserId != null
            }else{
                false
            }
        }
        val isAdded = page != null
        if(isAdded){
            (application as MiCore).removePageInCurrentAccount(page!!)
        }else{
            (application as MiApplication).addPageInCurrentAccount(
                Page(mAccountRelation?.accountId?: - 1,
                    title = user.getDisplayUserName(),
                    weight = -1,
                    pageable = Pageable.UserTimeline(userId = user.id.id)
                )
            )


        }

    }
}
