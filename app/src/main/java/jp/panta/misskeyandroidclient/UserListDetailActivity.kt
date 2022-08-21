@file:Suppress("DEPRECATION")

package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityUserListDetailBinding
import net.pantasystem.milktea.common_viewmodel.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.list.UserListDetailFragment
import jp.panta.misskeyandroidclient.ui.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.ui.list.viewmodel.UserListDetailViewModel
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_navigation.ChangedDiffResult
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class UserListDetailActivity : AppCompatActivity(), UserListEditorDialog.OnSubmittedListener {

    companion object {
        private const val TAG = "UserListDetailActivity"
        private const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_LIST_ID"


        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_EDIT_NAME = "ACTION_EDIT_NAME"


        fun newIntent(context: Context, listId: UserList.Id): Intent {
            return Intent(context, UserListDetailActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, listId)
            }
        }
    }

    private var account: Account? = null
    private var mListId: UserList.Id? = null

    @Inject
    lateinit var assistedFactory: UserListDetailViewModel.ViewModelAssistedFactory
    private val accountViewModel: AccountViewModel by viewModels()

    @Inject lateinit var settingStore: SettingStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory


    @FlowPreview
    @ExperimentalCoroutinesApi
    val mUserListDetailViewModel: UserListDetailViewModel by viewModels {
        val listId = intent.getSerializableExtra(EXTRA_LIST_ID) as UserList.Id
        UserListDetailViewModel.provideFactory(assistedFactory, listId)
    }

    private var mUserListName: String = ""
    private val binding: ActivityUserListDetailBinding by dataBinding()
    val notesViewModel by viewModels<NotesViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_user_list_detail)

        setSupportActionBar(binding.userListToolbar)

        val listId = intent.getSerializableExtra(EXTRA_LIST_ID) as UserList.Id


        mListId = listId


        net.pantasystem.milktea.note.view.ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        ).initViewModelListener()

        binding.userListDetailViewPager.adapter = PagerAdapter(listId)
        binding.userListDetailTab.setupWithViewPager(binding.userListDetailViewPager)

        mUserListDetailViewModel.userList.observe(this) { ul ->
            supportActionBar?.title = ul.name
            mUserListName = ul.name



            if (intent.action == ACTION_EDIT_NAME) {
                intent.action = ACTION_SHOW
                showEditUserListDialog()
            }
        }

        mUserListDetailViewModel.userList.observe(this) {
            invalidateOptionsMenu()
        }


    }

    override fun onSubmit(name: String) {
        mUserListDetailViewModel.updateName(name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_list_detail, menu)
        val addToTabItem = menu.findItem(R.id.action_add_to_tab)
        val page = account?.pages?.firstOrNull {
            (it.pageable() as? Pageable.UserListTimeline)?.listId == mListId?.userListId && mListId != null
        }
        if (page == null) {
            addToTabItem?.setIcon(R.drawable.ic_add_to_tab_24px)
        } else {
            addToTabItem?.setIcon(R.drawable.ic_remove_to_tab_24px)
        }
        setMenuTint(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_to_tab -> {
                toggleAddToTab()
            }
            R.id.action_update_list_user -> {
                showEditUserListDialog()
            }
            android.R.id.home -> {
                finish()
            }
            R.id.action_add_user -> {
                val selected = mUserListDetailViewModel.listUsers.value?.mapNotNull {
                    it.userId
                } ?: return false
                val intent = SearchAndSelectUserActivity.newIntent(this, selectedUserIds = selected)
                requestSelectUserResult.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    @ExperimentalCoroutinesApi
    @FlowPreview
    val requestSelectUserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == RESULT_OK) {
                val changedDiff =
                    data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? ChangedDiffResult
                val added = changedDiff?.added
                val removed = changedDiff?.removed
                Log.d(TAG, "新たに追加:${added?.toList()}, 削除:${removed?.toList()}")
                added?.forEach {
                    mUserListDetailViewModel.pushUser(it)
                }
                removed?.forEach {
                    mUserListDetailViewModel.pullUser(it)
                }
            }
        }

    private fun showEditUserListDialog() {
        val listId = mListId ?: return
        val dialog = UserListEditorDialog.newInstance(listId.userListId, mUserListName)
        dialog.show(supportFragmentManager, "")
    }


    private fun toggleAddToTab() {
        val page = account?.pages?.firstOrNull {
            val pageable = it.pageable()
            if (pageable is Pageable.UserListTimeline) {
                pageable.listId == mListId?.userListId && mListId != null
            } else {
                false
            }
        }
        if (page == null) {
            accountViewModel.addPage(
                Page(
                    account?.accountId ?: -1,
                    mUserListName,
                    weight = -1,
                    pageable = Pageable.UserListTimeline(
                        mListId?.userListId!!
                    )
                )
            )
        } else {
            accountViewModel.removePage(page)
        }
    }


    inner class PagerAdapter(val listId: UserList.Id) :
        FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val titles = listOf(getString(R.string.timeline), getString(R.string.user_list))
        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    pageableFragmentFactory.create(
                        Pageable.UserListTimeline(listId = listId.userListId)
                    )
                }
                1 -> {
                    UserListDetailFragment()
                }
                else -> throw IllegalArgumentException("max 2 page")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }


}
