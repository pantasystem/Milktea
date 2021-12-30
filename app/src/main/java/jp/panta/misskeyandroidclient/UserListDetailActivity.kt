package jp.panta.misskeyandroidclient

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.databinding.ActivityUserListDetailBinding
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.view.list.UserListDetailFragment
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.view.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class UserListDetailActivity : AppCompatActivity(), UserListEditorDialog.OnSubmittedListener {

    companion object {
        private const val TAG = "UserListDetailActivity"
        private const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_LIST_ID"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.extra.ACCOUNT_ID"

        private const val SELECT_USER_REQUEST_CODE = 252

        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_EDIT_NAME = "ACTION_EDIT_NAME"

        const val EXTRA_UPDATED_USER_LIST = "EXTRA_UPDATED_USER_LIST"

        fun newIntent(context: Context, listId: UserList.Id): Intent {
            return Intent(context, UserListDetailActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, listId)
            }
        }
    }

    private var account: Account? = null
    private var mListId: UserList.Id? = null

    @FlowPreview
    @ExperimentalCoroutinesApi
    private var mUserListDetailViewModel: UserListDetailViewModel? = null

    private var mIsNameUpdated: Boolean = false
    private var mUserListName: String = ""
    private val binding: ActivityUserListDetailBinding by dataBinding()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_user_list_detail)

        setSupportActionBar(binding.userListToolbar)

        val listId = intent.getSerializableExtra(EXTRA_LIST_ID) as UserList.Id
        //val accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)


        mListId = listId
        val miCore = application as MiCore
        val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(application as MiApplication))[NotesViewModel::class.java]

        val userListDetailViewModel = ViewModelProvider(this, UserListDetailViewModel.Factory(listId, miCore))[UserListDetailViewModel::class.java]
        mUserListDetailViewModel = userListDetailViewModel

        ActionNoteHandler(this, notesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java]).initViewModelListener()

        userListDetailViewModel.userList.observe(this, { ul ->
            supportActionBar?.title = ul.name
            mUserListName = ul.name

            binding.userListDetailViewPager.adapter = PagerAdapter(ul.id)
            binding.userListDetailTab.setupWithViewPager(binding.userListDetailViewPager)

            if(intent.action == ACTION_EDIT_NAME){
                intent.action = ACTION_SHOW
                showEditUserListDialog()
            }
        })

        userListDetailViewModel.userList.observe(this, {
            invalidateOptionsMenu()
        })



    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onSubmit(name: String) {
        mUserListDetailViewModel?.updateName(name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_list_detail, menu)
        val addToTabItem = menu.findItem(R.id.action_add_to_tab)
        val page = account?.pages?.firstOrNull {
            (it.pageable() as? Pageable.UserListTimeline)?.listId == mListId?.userListId && mListId != null
        }
        if(page == null){
            addToTabItem?.setIcon(R.drawable.ic_add_to_tab_24px)
        }else{
            addToTabItem?.setIcon(R.drawable.ic_remove_to_tab_24px)
        }
        setMenuTint(menu)
        return super.onCreateOptionsMenu(menu)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_to_tab ->{
                toggleAddToTab()
            }
            R.id.action_update_list_user ->{
                showEditUserListDialog()
            }
            android.R.id.home ->{
                if(mIsNameUpdated){
                    updatedResultFinish()
                }
            }
            R.id.action_add_user ->{
                val selected = mUserListDetailViewModel?.listUsers?.value?.map{
                    it.userId
                }?.filterNotNull()?: return false
                val intent = SearchAndSelectUserActivity.newIntent(this, selectedUserIds = selected)
                requestSelectUserResult.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onBackPressed() {
        updatedResultFinish()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    val requestSelectUserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val resultCode = result.resultCode
        val data = result.data
        if(resultCode == RESULT_OK){
            val changedDiff = data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult
            val added = changedDiff?.added
            val removed = changedDiff?.removed
            Log.d(TAG, "新たに追加:${added?.toList()}, 削除:${removed?.toList()}")
            added?.forEach{
                mUserListDetailViewModel?.pushUser(it)
            }
            removed?.forEach{
                mUserListDetailViewModel?.pullUser(it)
            }
        }
    }

    private fun showEditUserListDialog(){
        val listId = mListId?: return
        val dialog = UserListEditorDialog.newInstance(listId.userListId, mUserListName)
        dialog.show(supportFragmentManager, "")
    }


    private fun toggleAddToTab(){
        val page = account?.pages?.firstOrNull {
            val pageable = it.pageable()
            if(pageable is Pageable.UserListTimeline){
                pageable.listId == mListId?.userListId && mListId != null
            }else{
                false
            }
        }
        val miCore = application as MiCore
        if(page == null){
            miCore.addPageInCurrentAccount(
                Page(account?.accountId?: - 1, mUserListName, weight = -1, pageable = Pageable.UserListTimeline(mListId?.userListId!!))
            )
        }else{
            miCore.removePageInCurrentAccount(page)
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun updatedResultFinish(){
        val updatedEvent = mUserListDetailViewModel?.updateEvents?.toList()?: emptyList()

        val data = Intent().apply{
            if(updatedEvent.isNotEmpty()){
                putExtra(EXTRA_UPDATED_USER_LIST, mUserListDetailViewModel?.userList?.value)
            }
        }
        if(mListId == null || updatedEvent.isEmpty()){
            setResult(RESULT_CANCELED)
        }else{
            setResult(RESULT_OK, data)
        }
        finish()
    }



    inner class PagerAdapter(val listId: UserList.Id) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        private val titles = listOf(getString(R.string.timeline), getString(R.string.user_list))
        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->{
                    TimelineFragment.newInstance(
                        Pageable.UserListTimeline(listId = listId.userListId)
                    )
                }
                1 ->{
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
