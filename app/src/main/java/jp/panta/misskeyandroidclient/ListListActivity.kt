package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.view.list.ListListAdapter
import jp.panta.misskeyandroidclient.view.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListPullPushUserViewModel
import kotlinx.android.synthetic.main.activity_list_list.*
import kotlinx.android.synthetic.main.content_list_list.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ListListActivity : AppCompatActivity(), ListListAdapter.OnTryToEditCallback, UserListEditorDialog.OnSubmittedListener{

    companion object{
        const val EXTRA_USER_LIST_NAME = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_NAME"
        const val EXTRA_USER_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_ID"
        const val EXTRA_CREATED_AT = "jp.panta.misskeyandroidclient.EXTRA_CREATED_AT"
        const val EXTRA_USER_ID_ARRAY = "jp.panta.misskeyandroidclient.EXTRA_USER_ID_ARRAY"

        const val EXTRA_ADD_USER_ID = "jp.panta.misskeyandroidclient.extra.ADD_USER_ID"

        private const val USER_LIST_ACTIVITY_RESULT_CODE = 12
    }

    private var mListListViewModel: ListListViewModel? = null

    private var mPullPushUserViewModelEventDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_list_list)

        val addUserId = intent.getStringExtra(EXTRA_ADD_USER_ID)

        val miCore = application as MiCore

        val layoutManager = LinearLayoutManager(this)
        mListListViewModel = ViewModelProvider(this, ListListViewModel.Factory(miCore))[ListListViewModel::class.java]

        val listAdapter =
        if(addUserId == null){
            ListListAdapter(
                mListListViewModel!!,
                this,
                this
            )
        }else{
            val pullPushUserViewModel = ViewModelProvider(this, UserListPullPushUserViewModel.Factory(miCore))[UserListPullPushUserViewModel::class.java]

            miCore.getCurrentAccount().filterNotNull().onEach{
                pullPushUserViewModel.account.value = it
            }.launchIn(lifecycleScope)

            if(mPullPushUserViewModelEventDisposable?.isDisposed == true){
                mPullPushUserViewModelEventDisposable = pullPushUserViewModel.pullPushEvent.subscribe {
                    mListListViewModel?.loadListList()
                }
            }

            ListListAdapter(
                mListListViewModel!!,
                this,
                this,
                addUserId,
                pullPushUserViewModel
            )
        }


        listListView.adapter = listAdapter
        listListView.layoutManager = layoutManager
        mListListViewModel?.userListList?.observe(this, Observer{ userListList ->
            listAdapter.submitList(userListList)
        })
        mListListViewModel?.loadListList()

        setUpObservers()
        addListButton.setOnClickListener {
            val dialog = UserListEditorDialog.newInstance()
            dialog.show(supportFragmentManager, "")
        }
    }



    private fun setUpObservers(){
        mListListViewModel?.showUserDetailEvent?.removeObserver(showUserListDetail)
        mListListViewModel?.showUserDetailEvent?.observe(this, showUserListDetail)

    }

    private val showUserListDetail = Observer<UserList>{ ul ->
        val intent = Intent(this, UserListDetailActivity::class.java)
        intent.putExtra(UserListDetailActivity.EXTRA_LIST_ID, ul.id)
        intent.putExtra(UserListDetailActivity.EXTRA_ACCOUNT_ID, mListListViewModel?.account?.accountId?: - 1)
        startActivityForResult(intent, USER_LIST_ACTIVITY_RESULT_CODE)
    }



    override fun onEdit(userList: UserList?) {
        userList?: return

        val intent = Intent(this, UserListDetailActivity::class.java)
        val account = mListListViewModel?.account
        if(account != null){
            intent.putExtra(UserListDetailActivity.EXTRA_LIST_ID, userList.id)
                .putExtra(UserListDetailActivity.EXTRA_ACCOUNT_ID, account.accountId)
            intent.action = UserListDetailActivity.ACTION_EDIT_NAME
            startActivityForResult(intent, USER_LIST_ACTIVITY_RESULT_CODE)
        }
    }

    override fun onSubmit(name: String) {
        mListListViewModel?.createUserList(name)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            USER_LIST_ACTIVITY_RESULT_CODE ->{
                if(resultCode == RESULT_OK){
                    val updated = data?.getSerializableExtra(UserListDetailActivity.EXTRA_UPDATED_USER_LIST) as? UserList
                    if(updated != null){
                        mListListViewModel?.onUserListUpdated(updated)
                    }

                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        mPullPushUserViewModelEventDisposable?.dispose()
        mPullPushUserViewModelEventDisposable = null
    }
}
