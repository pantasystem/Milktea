package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.app.TaskStackBuilder
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.databinding.ActivityUserDetailBinding
import jp.panta.misskeyandroidclient.ui.notes.view.ActionNoteHandler
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineFragment
import jp.panta.misskeyandroidclient.ui.users.PinNoteFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserDetailViewModel
import java.lang.IllegalArgumentException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountStore
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.gallery.GalleryPostsFragment
import jp.panta.misskeyandroidclient.ui.users.ReportDialog
import jp.panta.misskeyandroidclient.ui.users.nickname.EditNicknameDialog
import jp.panta.misskeyandroidclient.ui.users.viewmodel.provideFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class UserDetailActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_USER_ID =
            "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_ID"
        private const val EXTRA_USER_NAME =
            "jp.panta.misskeyandroidclient.UserDetailActivity.EXTRA_USER_NAME"
        private const val EXTRA_ACCOUNT_ID =
            "jp.panta.misskeyandroiclient.UserDetailActivity.EXTRA_ACCOUNT_ID"
        const val EXTRA_IS_MAIN_ACTIVE = "jp.panta.misskeyandroidclient.EXTRA_IS_MAIN_ACTIVE"

        fun newInstance(context: Context, userId: User.Id): Intent {
            return Intent(context, UserDetailActivity::class.java).apply {

                putExtra(EXTRA_USER_ID, userId.id)
                putExtra(EXTRA_ACCOUNT_ID, userId.accountId)
            }
        }

        fun newInstance(context: Context, userName: String): Intent {
            return Intent(context, UserDetailActivity::class.java).apply {
                putExtra(EXTRA_USER_NAME, userName)

            }
        }
    }

    @Inject
    lateinit var assistedFactory: UserDetailViewModel.ViewModelAssistedFactory

    @Inject
    lateinit var accountStore: AccountStore

    private val accountViewModel: AccountViewModel by viewModels()


    @ExperimentalCoroutinesApi
    val mViewModel: UserDetailViewModel by viewModels {
        val remoteUserId: String? = intent.getStringExtra(EXTRA_USER_ID)
        val accountId: Long = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)
        if (!(remoteUserId == null || accountId == -1L)) {
            val userId = User.Id(accountId, remoteUserId)
            return@viewModels UserDetailViewModel.provideFactory(assistedFactory, userId)
        }
        val userName = intent.data?.getQueryParameter("userName")
            ?: intent.getStringExtra(EXTRA_USER_NAME)
            ?: intent.data?.path?.let { path ->
                if (path.startsWith("/")) {
                    path.substring(1, path.length)
                } else {
                    path
                }
            }
        return@viewModels UserDetailViewModel.provideFactory(assistedFactory, userName!!)
    }


    private var mUserId: User.Id? = null
    private var mIsMainActive: Boolean = true

    private var mParentActivity: Activities? = null
    val notesViewModel by viewModels<NotesViewModel>()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(
            this,
            R.layout.activity_user_detail
        )
        binding.lifecycleOwner = this
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mParentActivity = intent.getParentActivity()

        val remoteUserId: String? = intent.getStringExtra(EXTRA_USER_ID)
        val accountId: Long = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)
        val userId: User.Id? = if (!(remoteUserId == null || accountId == -1L)) {
            User.Id(accountId, remoteUserId)
        } else {
            null
        }
        mUserId = userId

        val userName = intent.data?.getQueryParameter("userName")
            ?: intent.getStringExtra(EXTRA_USER_NAME)
            ?: intent.data?.path?.let { path ->
                if (path.startsWith("/")) {
                    path.substring(1, path.length)
                } else {
                    path
                }
            }
        Log.d("UserDetailActivity", "userName:$userName")
        mIsMainActive = intent.getBooleanExtra(EXTRA_IS_MAIN_ACTIVE, true)

        val miApplication = applicationContext as MiApplication

        ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java]
        )
            .initViewModelListener()

        miApplication.getAccountStore().observeCurrentAccount.filterNotNull().onEach { ar ->

            binding.userViewModel = mViewModel

            val isEnableGallery =
                miApplication.getMisskeyAPIProvider().get(ar.instanceDomain) is MisskeyAPIV1275
            mViewModel.load()
            mViewModel.user.observe(this, { detail ->
                if (detail != null) {
                    val adapter = UserTimelinePagerAdapterV2(ar, detail.id.id, isEnableGallery)
                    binding.userTimelinePager.adapter = adapter

                    TabLayoutMediator(
                        binding.userTimelineTab,
                        binding.userTimelinePager
                    ) { tab, position ->
                        tab.text = adapter.titles[position]
                    }.attach()
                    supportActionBar?.title = detail.getDisplayUserName()
                }

            })


            mViewModel.userName.observe(this, {
                supportActionBar?.title = it
            })
            //userTimelineTab.setupWithViewPager()
            mViewModel.showFollowers.observe(this, {
                it?.let {
                    val intent = FollowFollowerActivity.newIntent(this, it.id, isFollowing = false)
                    startActivity(intent)
                }
            })

            mViewModel.showFollows.observe(this, {
                it?.let {
                    val intent = FollowFollowerActivity.newIntent(this, it.id, true)
                    startActivity(intent)
                }
            })

            val updateMenu = Observer<Boolean> {
                invalidateOptionsMenu()
            }
            mViewModel.isBlocking.observe(this, updateMenu)
            mViewModel.isMuted.observe(this, updateMenu)

            invalidateOptionsMenu()


            binding.showRemoteUser.setOnClickListener {
                mViewModel.user.value?.url?.let {
                    val uri = Uri.parse(it)
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                }
            }

            binding.createMention.setOnClickListener {
                mViewModel.user.value?.getDisplayUserName()?.let {
                    val intent = NoteEditorActivity.newBundle(this, mentions = listOf(it))
                    startActivity(intent)
                }

            }

        }.launchIn(lifecycleScope)

        binding.editNicknameButton.setOnClickListener {
            EditNicknameDialog().show(supportFragmentManager, "editNicknameDialog")
        }


    }

    inner class UserTimelinePagerAdapterV2(
        val account: Account,
        val userId: String,
        enableGallery: Boolean = false
    ) : FragmentStateAdapter(this) {
        val titles = if (enableGallery) listOf(
            getString(R.string.post),
            getString(R.string.pin),
            getString(R.string.media),
            getString(R.string.gallery)
        ) else listOf(getString(R.string.post), getString(R.string.pin), getString(R.string.media))
        private val requestTimeline = Pageable.UserTimeline(userId)
        private val requestMedia = Pageable.UserTimeline(userId, withFiles = true)

        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TimelineFragment.newInstance(requestTimeline)
                1 -> PinNoteFragment.newInstance(userId = User.Id(account.accountId, userId), null)
                2 -> TimelineFragment.newInstance(requestMedia)
                3 -> GalleryPostsFragment.newInstance(
                    Pageable.Gallery.User(userId),
                    account.accountId
                )
                else -> throw IllegalArgumentException("こんなものはない！！")
            }
        }

        override fun getItemCount(): Int {
            return titles.size
        }

    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_user_menu, menu)

        val block = menu.findItem(R.id.block)
        val mute = menu.findItem(R.id.mute)
        val unblock = menu.findItem(R.id.unblock)
        val unmute = menu.findItem(R.id.unmute)
        val report = menu.findItem(R.id.report_user)
        mute?.isVisible = !(mViewModel.isMuted.value ?: true)
        block?.isVisible = !(mViewModel.isBlocking.value ?: true)
        unblock?.isVisible = mViewModel.isBlocking.value ?: false
        unmute?.isVisible = mViewModel.isMuted.value ?: false
        if (mViewModel.isMine.value == true) {
            block?.isVisible = false
            mute?.isVisible = false
            unblock?.isVisible = false
            unmute?.isVisible = false
            report?.isVisible = false
        }

        val tab = menu.findItem(R.id.nav_add_to_tab)
        val page = accountStore.currentAccount?.pages?.firstOrNull {
            val pageable = it.pageable()
            if (pageable is Pageable.UserTimeline) {
                pageable.userId == mUserId?.id
            } else {
                false
            }
        }
        if (page == null) {
            tab?.setIcon(R.drawable.ic_add_to_tab_24px)
        } else {
            tab?.setIcon(R.drawable.ic_remove_to_tab_24px)
        }

        setMenuTint(menu)

        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("UserDetail", "mParentActivity: $mParentActivity")

        when (item.itemId) {
            android.R.id.home -> {
                finishAndGoToMainActivity()
                return true
            }
            R.id.block -> {
                mViewModel.block()
            }
            R.id.mute -> {
                mViewModel.mute()
            }
            R.id.unblock -> {
                mViewModel.unblock()
            }
            R.id.unmute -> {
                mViewModel.unmute()
            }
            R.id.nav_add_to_tab -> {
                addPageToTab()
            }
            R.id.add_list -> {
                val intent = ListListActivity.newInstance(this, mUserId)
                startActivity(intent)
            }
            R.id.report_user -> {
                mUserId?.let {
                    ReportDialog.newInstance(it).show(supportFragmentManager, "")
                }
            }
            R.id.share -> {
                val url = mViewModel.profileUrl.value
                    ?: return false

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
            else -> return false

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun finishAndGoToMainActivity() {
        if (mParentActivity == null || mParentActivity == Activities.ACTIVITY_OUT_APP) {
            val upIntent = Intent(this, MainActivity::class.java)
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            if (shouldUpRecreateTask(upIntent)) {
                TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
                finish()
            } else {
                navigateUpTo(upIntent)
            }


            return
        }
        finish()
    }


    @ExperimentalCoroutinesApi
    private fun addPageToTab() {
        val user = mViewModel.user.value
        user ?: return

        val page = accountStore.currentAccount?.pages?.firstOrNull {
            val pageable = it.pageable()
            if (pageable is Pageable.UserTimeline) {
                pageable.userId == mUserId?.id && mUserId != null
            } else {
                false
            }
        }
        val isAdded = page != null
        if (isAdded) {
            accountViewModel.removePage(page!!)
        } else {
            accountViewModel.addPage(
                Page(
                    accountStore.currentAccountId ?: -1,
                    title = user.getDisplayUserName(),
                    weight = -1,
                    pageable = Pageable.UserTimeline(userId = user.id.id)
                )
            )


        }

    }
}
