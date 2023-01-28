package net.pantasystem.milktea.user.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull
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
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.Theme
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.NoteEditorActivity
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.activity.binder.UserDetailActivityMenuBinder
import net.pantasystem.milktea.user.databinding.ActivityUserDetailBinding
import net.pantasystem.milktea.user.nickname.EditNicknameDialog
import net.pantasystem.milktea.user.profile.ConfirmUserBlockDialog
import net.pantasystem.milktea.user.profile.UserProfileFieldListAdapter
import net.pantasystem.milktea.user.profile.mute.SpecifyMuteExpiredAtDialog
import net.pantasystem.milktea.user.reaction.UserReactionsFragment
import net.pantasystem.milktea.user.viewmodel.UserDetailTabType
import net.pantasystem.milktea.user.viewmodel.UserDetailViewModel
import net.pantasystem.milktea.user.viewmodel.provideFactory
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
        private const val EXTRA_USER_ID =
            "net.pantasystem.milktea.user.activity.UserDetailActivity.EXTRA_USER_ID"
        private const val EXTRA_USER_NAME =
            "net.pantasystem.milktea.user.activity.UserDetailActivity.EXTRA_USER_NAME"
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

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory


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

    @OptIn(ExperimentalCoroutinesApi::class)
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

        ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        )
            .initViewModelListener()

        val adapter = UserTimelinePagerAdapterV2(
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

        mViewModel.showFollowers.observe(this) {
            if (it != null) {
                supportActionBar?.title = it.displayUserName
            }

            it?.let {
                val intent = FollowFollowerActivity.newIntent(this, it.id, isFollowing = false)
                startActivity(intent)
            }
        }

        mViewModel.showFollows.observe(this) {
            it?.let {
                val intent = FollowFollowerActivity.newIntent(this, it.id, true)
                startActivity(intent)
            }
        }


        lifecycleScope.launch {
            mViewModel.userState.collect {
                invalidateOptionsMenu()
                supportActionBar?.title = it?.displayUserName
            }
        }

        invalidateOptionsMenu()


        binding.showRemoteUser.setOnClickListener {
            val account = accountStore.currentAccount
            if (account != null) {
                mViewModel.user.value?.getProfileUrl(account)?.let {
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

                mViewModel.user.value?.getRemoteProfileUrl(account)?.let {
                    val uri = Uri.parse(it)
                    startActivity(
                        Intent(Intent.ACTION_VIEW, uri)
                    )
                }
            }
        }

        binding.createMention.setOnClickListener {
            mViewModel.user.value?.displayUserName?.let {
                val intent = NoteEditorActivity.newBundle(this, mentions = listOf(it))
                startActivity(intent)
            }

        }


        binding.editNicknameButton.setOnClickListener {
            EditNicknameDialog().show(supportFragmentManager, "editNicknameDialog")
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

        lifecycleScope.launch {
            mViewModel.errors.collect {
                withResumed {
                    Toast.makeText(
                        this@UserDetailActivity,
                        getString(R.string.error_s, it),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }


    @OptIn(ExperimentalCoroutinesApi::class)
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
                ConfirmUserBlockDialog().show(supportFragmentManager, "ConfirmUserBlockDialog")
            }
            R.id.mute -> {
                SpecifyMuteExpiredAtDialog()
                    .show(supportFragmentManager, "SpecifyMuteExpiredAtDialog")
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
                    ReportDialog.newInstance(it).show(supportFragmentManager, "")
                }
            }
            R.id.share -> {
                val account = accountStore.currentAccount
                val url = account?.let {
                    mViewModel.user.value?.getRemoteProfileUrl(it)
                } ?: return false


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
        config: Config
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
}

class UserTimelinePagerAdapterV2(
    val pageableFragmentFactory: PageableFragmentFactory,
    val userPinnedNotesFragmentFactory: UserPinnedNotesFragmentFactory,
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {

    var tabs: List<UserDetailTabType> = emptyList()
        private set

    override fun createFragment(position: Int): Fragment {
        return when (val tab = tabs[position]) {
            is UserDetailTabType.Gallery -> pageableFragmentFactory.create(
                tab.accountId,
                Pageable.Gallery.User(tab.userId.id),
            )
            is UserDetailTabType.Media -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    withFiles = true
                )
            )
            is UserDetailTabType.PinNote -> userPinnedNotesFragmentFactory.create(tab.userId)
            is UserDetailTabType.Reactions -> UserReactionsFragment.newInstance(tab.userId)
            is UserDetailTabType.UserTimeline -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = false
                )
            )
            is UserDetailTabType.UserTimelineWithReplies -> pageableFragmentFactory.create(
                Pageable.UserTimeline(
                    tab.userId.id,
                    includeReplies = true
                )
            )
            is UserDetailTabType.MastodonMedia -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    isOnlyMedia = true,
                )
            )
            is UserDetailTabType.MastodonUserTimeline -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = true,
                )
            )
            is UserDetailTabType.MastodonUserTimelineWithReplies -> pageableFragmentFactory.create(
                Pageable.Mastodon.UserTimeline(
                    tab.userId.id,
                    excludeReplies = false,
                )
            )
        }

    }

    override fun getItemCount(): Int {
        return tabs.size
    }

    fun submitList(list: List<UserDetailTabType>) {

        val old = tabs
        tabs = list
        val callback = object : DiffUtil.Callback() {
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }

            override fun getNewListSize(): Int {
                return list.size
            }

            override fun getOldListSize(): Int {
                return old.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }
        }
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }


}
