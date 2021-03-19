package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
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
import kotlinx.android.synthetic.main.activity_user_list_detail.*

class UserListDetailActivity : AppCompatActivity(), UserListEditorDialog.OnSubmittedListener {

    companion object {
        private const val TAG = "UserListDetailActivity"
        const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_LIST_ID"
        const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.extra.ACCOUNT_ID"

        private const val SELECT_USER_REQUEST_CODE = 252

        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_EDIT_NAME = "ACTION_EDIT_NAME"

        const val EXTRA_UPDATED_USER_LIST = "EXTRA_UPDATED_USER_LIST"
    }

    private var account: Account? = null
    private var mListId: String? = null
    private var mUserListDetailViewModel: UserListDetailViewModel? = null

    private var mIsNameUpdated: Boolean = false
    private var mUserListName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_user_list_detail)

        setSupportActionBar(userListToolbar)

        val listId = intent.getStringExtra(EXTRA_LIST_ID)
        val accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)

        if(accountId < 0 || listId == null){
            Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show()
            return finish()
        }

        mListId = listId
        val miCore = application as MiCore
        val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(application as MiApplication))[NotesViewModel::class.java]

        val userListDetailViewModel = ViewModelProvider(this, UserListDetailViewModel.Factory(accountId, listId, miCore))[UserListDetailViewModel::class.java]
        mUserListDetailViewModel = userListDetailViewModel

        ActionNoteHandler(this, notesViewModel, ViewModelProvider(this)[ConfirmViewModel::class.java]).initViewModelListener()

        userListDetailViewModel.userList.observe(this, Observer<UserList>{ ul ->
            supportActionBar?.title = ul.name
            mUserListName = ul.name

            userListDetailViewPager.adapter = PagerAdapter(ul.id)
            userListDetailTab.setupWithViewPager(userListDetailViewPager)

            if(intent.action == ACTION_EDIT_NAME){
                intent.action = ACTION_SHOW
                showEditUserListDialog()
            }
        })

        userListDetailViewModel.userList.observe(this, Observer {
            invalidateOptionsMenu()
        })



    }

    override fun onSubmit(name: String) {
        mUserListDetailViewModel?.updateName(name)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_list_detail, menu)
        val addToTabItem = menu?.findItem(R.id.action_add_to_tab)
        val page = account?.pages?.firstOrNull {
            (it.pageable() as? Pageable.UserListTimeline)?.listId == mListId && mListId != null
        }
        if(page == null){
            addToTabItem?.setIcon(R.drawable.ic_add_to_tab_24px)
        }else{
            addToTabItem?.setIcon(R.drawable.ic_remove_to_tab_24px)
        }
        menu?.let{
            setMenuTint(it)
        }
        return super.onCreateOptionsMenu(menu)
    }

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
                startActivityForResult(intent, SELECT_USER_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        updatedResultFinish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: reqCode:$requestCode, resultCode:$resultCode")
        if(requestCode == SELECT_USER_REQUEST_CODE){
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
    }

    private fun showEditUserListDialog(){
        val listId = mListId?: return
        val dialog = UserListEditorDialog.newInstance(listId, mUserListName)
        dialog.show(supportFragmentManager, "")
    }


    private fun toggleAddToTab(){
        val page = account?.pages?.firstOrNull {
            val pageable = it.pageable()
            if(pageable is Pageable.UserListTimeline){
                pageable.listId == mListId && mListId != null
            }else{
                false
            }
        }
        val miCore = application as MiCore
        if(page == null){
            miCore.addPageInCurrentAccount(
                Page(account?.accountId?: - 1, mUserListName, weight = -1, pageable = Pageable.UserListTimeline(mListId!!))
            )
        }else{
            miCore.removePageInCurrentAccount(page)
        }
    }

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

    private fun createdResultFinish(userList: UserList){
        val data = Intent().apply{
            putExtra(ListListActivity.EXTRA_USER_LIST_ID, userList.id)
            putExtra(ListListActivity.EXTRA_USER_LIST_NAME, userList.name)
            putExtra(ListListActivity.EXTRA_CREATED_AT, userList.createdAt)
            putExtra(ListListActivity.EXTRA_USER_ID_ARRAY, userList.userIds.toTypedArray())
        }
        setResult(RESULT_OK, data)
        finish()
    }

    inner class PagerAdapter(val listId: String) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        private val titles = listOf(getString(R.string.timeline), getString(R.string.user_list))
        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->{
                    TimelineFragment.newInstance(
                        Pageable.UserListTimeline(listId = listId)
                    )
                }
                1 ->{
                    UserListDetailFragment()
                }
                else -> throw IllegalArgumentException("max 2 page")
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }
    }





}
