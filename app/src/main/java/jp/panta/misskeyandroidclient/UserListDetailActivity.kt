package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.list.UserListDetailFragment
import jp.panta.misskeyandroidclient.view.notes.ActionNoteHandler
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListOperateViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import kotlinx.android.synthetic.main.activity_user_list_detail.*

class UserListDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserListDetailActivity"
        const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_LIST_ID"

        private const val SELECT_USER_REQUEST_CODE = 252
    }

    private var mAccountRelation: AccountRelation? = null
    private var mListId: String? = null
    private var mUserListDetailViewModel: UserListDetailViewModel? = null
    private var mUserListOperateViewModelProvider: UserListOperateViewModel? = null

    private var mIsNameUpdated: Boolean = false
    private var mUserListName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_user_list_detail)

        setSupportActionBar(userListToolbar)

        val listId = intent.getStringExtra(EXTRA_LIST_ID)
        mListId = listId
        val miCore = application as MiCore
        miCore.currentAccount.observe(this, Observer{ ar ->
            val notesViewModel = ViewModelProvider(this, NotesViewModelFactory(ar, application as MiApplication))[NotesViewModel::class.java]
            ActionNoteHandler(this, notesViewModel).initViewModelListener()

            mAccountRelation = ar
            val userListDetailViewModel = ViewModelProvider(this, UserListDetailViewModel.Factory(ar, listId, miCore))[UserListDetailViewModel::class.java]
            mUserListDetailViewModel = userListDetailViewModel

            mUserListOperateViewModelProvider = ViewModelProvider(this, UserListOperateViewModel.Factory(ar, miCore))[UserListOperateViewModel::class.java]

            userListDetailViewModel.userList.observe(this, Observer<UserList>{ ul ->
                supportActionBar?.title = ul.name
                mUserListName = ul.name

                userListDetailViewPager.adapter = PagerAdapter(ul.id)
                userListDetailTab.setupWithViewPager(userListDetailViewPager)
            })
            invalidateOptionsMenu()

            userListDetailViewModel.load()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_list_detail, menu)
        val addToTabItem = menu?.findItem(R.id.action_add_to_tab)
        val page = mAccountRelation?.pages?.firstOrNull {
            it.listId == mListId && mListId != null
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_add_to_tab ->{
                toggleAddToTab()
            }
            R.id.action_update_list_user ->{
                showEditUserListDialog()
            }
            android.R.id.home ->{
                if(mIsNameUpdated){
                    updatedResultFinish(mUserListDetailViewModel?.userList?.value?.name)
                }
            }
            R.id.action_add_user ->{
                val intent = Intent(this, SearchAndSelectUserActivity::class.java)
                val selected = mUserListDetailViewModel?.listUsers?.value?.map{
                    it.userId
                }?.toTypedArray()?: return false
                intent.putExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_IDS, selected)
                startActivityForResult(intent, SELECT_USER_REQUEST_CODE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: reqCode:$requestCode, resultCode:$resultCode")
        if(requestCode == SELECT_USER_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                val added = data?.getStringArrayExtra(SearchAndSelectUserActivity.EXTRA_ADDED_USER_IDS)
                val removed = data?.getStringArrayExtra(SearchAndSelectUserActivity.EXTRA_REMOVED_USER_IDS)
                Log.d(TAG, "新たに追加:${added?.toList()}, 削除:${removed?.toList()}")
                val userList = mUserListDetailViewModel?.userList?.value?: return
                added?.forEach{
                    mUserListOperateViewModelProvider?.pushUser(userList, it)
                }
                removed?.forEach{
                    mUserListOperateViewModelProvider?.pullUser(userList.id, it)
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
        val page = mAccountRelation?.pages?.firstOrNull {
            it.listId == mListId && mListId != null
        }
        val miCore = application as MiCore
        if(page == null){
            miCore.addPageInCurrentAccount(
                NoteRequest.Setting(
                    type = NoteType.USER_LIST,
                    listId = mListId
                ).apply{
                    title = mUserListName
                }
            )
        }else{
            miCore.removePageInCurrentAccount(page)
        }
    }

    private fun updatedResultFinish(name: String?){
        val data = Intent().apply{
            putExtra(ListListActivity.EXTRA_USER_LIST_NAME, name)
            putExtra(ListListActivity.EXTRA_USER_LIST_ID, mListId)
            action = ListListActivity.ACTION_USER_LIST_UPDATED
        }
        if(mListId == null || name == null){
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
                        NoteRequest.Setting(
                            type = NoteType.USER_LIST,
                            listId = listId
                        )
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
