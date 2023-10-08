package net.pantasystem.milktea.user.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android.ui.Activities
import net.pantasystem.milktea.common_android.ui.getParentActivity
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import net.pantasystem.milktea.common_android_ui.report.ReportDialog
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.view.NoteActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.databinding.ActivityUserDetailBinding
import net.pantasystem.milktea.user.followlist.FollowFollowerActivity
import net.pantasystem.milktea.user.nickname.EditNicknameDialog
import net.pantasystem.milktea.user.profile.mute.SpecifyMuteExpiredAtDialog
import net.pantasystem.milktea.user.profile.viewmodel.UserDetailViewModel
import net.pantasystem.milktea.user.qrshare.QRShareDialog
import javax.inject.Inject

class UserDetailNavigationImpl @Inject constructor(
    val activity: Activity,
) : UserDetailNavigation {

    override fun newIntent(args: UserDetailNavigationArgs): Intent {
        return when (args) {
            is UserDetailNavigationArgs.UserId -> UserDetailActivity.newInstance(
                activity,
                args.userId
            )
            is UserDetailNavigationArgs.UserName -> UserDetailActivity.newInstance(
                activity,
                args.userName
            )
        }
    }
}

@AndroidEntryPoint
class UserDetailActivity : AppCompatActivity() {
    companion object {
        internal const val EXTRA_USER_ID =
            "net.pantasystem.milktea.user.profile.UserDetailActivity.EXTRA_USER_ID"
        internal const val EXTRA_USER_NAME =
            "net.pantasystem.milktea.user.profile.UserDetailActivity.EXTRA_USER_NAME"
        internal const val EXTRA_ACCOUNT_ID =
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
    internal lateinit var accountStore: AccountStore

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    internal lateinit var searchNavigation: SearchNavigation


//    @ExperimentalCoroutinesApi
//    val mViewModel: UserDetailViewModel by viewModels {
//        val remoteUserId: String? = intent.getStringExtra(EXTRA_USER_ID)
//        val accountId: Long = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)
//        if (!(remoteUserId == null || accountId == -1L)) {
//            val userId = User.Id(accountId, remoteUserId)
//            return@viewModels UserDetailViewModel.provideFactory(assistedFactory, userId)
//        }
//        val userName = intent.data?.getQueryParameter("userName")
//            ?: intent.getStringExtra(EXTRA_USER_NAME)
//            ?: intent.data?.path?.let { path ->
//                if (path.startsWith("/")) {
//                    path.substring(1, path.length)
//                } else {
//                    path
//                }
//            }
//        return@viewModels UserDetailViewModel.provideFactory(assistedFactory, userName!!)
//    }

    private val mViewModel: UserDetailViewModel by viewModels()


    private var mUserId: User.Id? = null
    private var mIsMainActive: Boolean = true

    private var mParentActivity: Activities? = null
    private val notesViewModel by viewModels<NotesViewModel>()

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var configRepository: LocalConfigRepository

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var applyMenuTint: ApplyMenuTint

    @Inject
    lateinit var mainActivityNavigation: MainNavigation

    @Inject
    lateinit var userListNavigation: UserListNavigation

    @Inject
    lateinit var userPinnedNotesFragmentFactory: UserPinnedNotesFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        val binding = DataBindingUtil.setContentView<ActivityUserDetailBinding>(
            this,
            R.layout.activity_user_detail
        )
        binding.lifecycleOwner = this
        binding.userViewModel = mViewModel
        setSupportActionBar(binding.userDetailToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
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

        NoteActionHandler(
            this.supportFragmentManager,
            this,
            this,
            notesViewModel,
        ).initViewModelListener()

        val adapter = ProfileTabPagerAdapter(
            pageableFragmentFactory,
            userPinnedNotesFragmentFactory,
            this
        )
        binding.userTimelinePager.adapter = adapter

        TabLayoutMediator(
            binding.userTimelineTab,
            binding.userTimelinePager
        ) { tab, position ->
            tab.text = getString(adapter.tabs[position].title)
        }.attach()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mViewModel.tabTypes.collect { tabs ->
                    binding.userTimelineTab.tabMode = if (tabs.size > 4) {
                        TabLayout.MODE_SCROLLABLE
                    } else {
                        TabLayout.MODE_FIXED
                    }
                    adapter.submitList(tabs)
                }
            }
        }

        binding.followsText.setOnClickListener {
            showFollowings()
        }

        binding.followingCounter.setOnClickListener {
            showFollowings()
        }

        binding.followersCounter.setOnClickListener {
            showFollowers()
        }

        binding.followersText.setOnClickListener {
            showFollowers()
        }

        lifecycleScope.launch {
            mViewModel.userState.collect {
                invalidateOptionsMenu()
                supportActionBar?.title = it?.displayUserName
            }
        }

        mViewModel.renoteMuteState.onEach {
            invalidateOptionsMenu()
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)

        invalidateOptionsMenu()


        binding.showRemoteUser.setOnClickListener {
            val account = accountStore.currentAccount
            if (account != null) {
                mViewModel.userState.value?.getProfileUrl(account)?.let {
                    val uri = Uri.parse(it)
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                }
            }
        }

        binding.showRemoteUserInRemotePage.setOnClickListener {
            val account = accountStore.currentAccount
            if (account != null) {

                mViewModel.userState.value?.getRemoteProfileUrl(account)?.let {
                    val uri = Uri.parse(it)
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                }
            }
        }

        binding.createMention.setOnClickListener {
            mViewModel.userState.value?.displayUserName?.let {
                val intent = NoteEditorActivity.newBundle(this, mentions = listOf(it))
                startActivity(intent)
            }

        }


        binding.editNicknameButton.setOnClickListener {
            EditNicknameDialog().show(supportFragmentManager, EditNicknameDialog.FRAGMENT_TAG)
        }


        val userFieldsAdapter = UserProfileFieldListAdapter()
        binding.userFields.adapter = userFieldsAdapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.userState.mapNotNull {
                    it?.info?.fields
                }.collect {
                    userFieldsAdapter.submitList(it)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                configRepository.observe().distinctUntilChangedBy {
                    it.theme
                }.collect {
                    applyRemoteUserStateLayoutBackgroundColor(binding, it)
                }
            }
        }

        mViewModel.errors.onEach {
            UserDetailErrorHandler(this@UserDetailActivity)(it)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_user_menu, menu)

        UserDetailActivityMenuBinder(this, mViewModel, applyMenuTint, accountStore)
            .bind(menu)

        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("UserDetail", "mParentActivity: $mParentActivity")

        when (item.itemId) {
            android.R.id.home -> {
                finishAndGoToMainActivity()
                return true
            }
            R.id.block -> {
                ConfirmUserBlockDialog().show(supportFragmentManager, ConfirmUserBlockDialog.FRAGMENT_TAG)
            }
            R.id.mute -> {
                SpecifyMuteExpiredAtDialog()
                    .show(supportFragmentManager, SpecifyMuteExpiredAtDialog.FRAGMENT_TAG)
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
                val intent = userListNavigation.newIntent(UserListArgs(mUserId))

                startActivity(intent)
            }
            R.id.report_user -> {
                mUserId?.let {
                    ReportDialog.newInstance(it).show(supportFragmentManager, ReportDialog.FRAGMENT_TAG)
                }
            }
            R.id.share -> {
                val account = accountStore.currentAccount
                val url = account?.let {
                    mViewModel.userState.value?.getRemoteProfileUrl(it)
                } ?: return false


                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, url)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
            R.id.renoteUnmute -> {
                mViewModel.unMuteRenotes()
            }
            R.id.renoteMute -> {
                mViewModel.muteRenotes()
            }
            R.id.nav_search_by_user -> {
                startActivity(searchNavigation.newIntent(
                    SearchNavType.SearchScreen(
                        acct = mViewModel.userState.value?.let {
                                "@${it.userName}@${it.host}"
                            }
                        )
                    )
                )
            }
            R.id.nav_switch_account -> {
                ProfileAccountSwitchDialog().show(supportFragmentManager, ProfileAccountSwitchDialog.FRAGMENT_TAG)
            }
            R.id.show_qr_code -> {
                QRShareDialog().show(supportFragmentManager, QRShareDialog.FRAGMENT_TAG)
            }
            R.id.notify_about_new_posts -> {
                mViewModel.toggleNotifyUserPosts()
            }
            R.id.stop_notify_about_new_posts -> {
                mViewModel.toggleNotifyUserPosts()
            }
            else -> return false

        }
        return super.onOptionsItemSelected(item)
    }


    private fun finishAndGoToMainActivity() {
        if (mParentActivity == null || mParentActivity == Activities.ACTIVITY_OUT_APP) {
            val upIntent = mainActivityNavigation.newIntent(Unit)
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

    private fun applyRemoteUserStateLayoutBackgroundColor(
        binding: ActivityUserDetailBinding,
        config: Config,
    ) {
        val typed = TypedValue()
        if (config.theme is Theme.Bread) {
            theme.resolveAttribute(R.attr.colorSurface, typed, true)
        } else {
            theme.resolveAttribute(R.attr.background, typed, true)
        }
        binding.remoteUserState.setBackgroundColor(typed.data)
    }


    @ExperimentalCoroutinesApi
    private fun addPageToTab() {
        mViewModel.toggleUserTimelineTab()
    }

    private fun showFollowers() {
        mViewModel.userState.value?.let {
            val intent = FollowFollowerActivity.newIntent(this, it.id, isFollowing = false)
            startActivity(intent)
        }
    }

    private fun showFollowings() {
        mViewModel.userState.value?.let {
            val intent = FollowFollowerActivity.newIntent(this, it.id, isFollowing = true)
            startActivity(intent)
        }
    }
}

