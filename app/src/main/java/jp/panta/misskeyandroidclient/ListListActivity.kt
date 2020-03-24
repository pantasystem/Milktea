package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.view.list.ListListAdapter
import jp.panta.misskeyandroidclient.viewmodel.list.DialogUserListEditor
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListOperateViewModel
import kotlinx.android.synthetic.main.activity_list_list.*
import kotlinx.android.synthetic.main.content_list_list.*

class ListListActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_USER_LIST_NAME = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_NAME"
        const val EXTRA_USER_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_USER_LIST_ID"
        const val EXTRA_CREATED_AT = "jp.panta.misskeyandroidclient.EXTRA_CREATED_AT"
        const val EXTRA_USER_ID_ARRAY = "jp.panta.misskeyandroidclient.EXTRA_USER_ID_ARRAY"
        const val ACTION_USER_LIST_CREATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_CREATED"
        const val ACTION_USER_LIST_UPDATED = "jp.panta.misskeyandroidclient.ACTION_USER_LIST_UPDATED"

        private const val USER_LIST_ACTIVITY_RESULT_CODE = 12
    }

    private var mListListViewModel: ListListViewModel? = null
    private var mListOperateViewModel: UserListOperateViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_list_list)

        val miCore = application as MiCore

        val layoutManager = LinearLayoutManager(this)
        miCore.currentAccount.observe(this, Observer{ ar ->
            mListListViewModel = ViewModelProvider(this, ListListViewModel.Factory(ar, miCore))[ListListViewModel::class.java]
            val userListOperateViewModel = ViewModelProvider(this, UserListOperateViewModel.Factory(ar, miCore))[UserListOperateViewModel::class.java]
            mListOperateViewModel = userListOperateViewModel
            val listAdapter =
                ListListAdapter(
                    mListListViewModel!!,
                    this,
                    userListOperateViewModel
                    )
            listListView.adapter = listAdapter
            listListView.layoutManager = layoutManager
            mListListViewModel?.userListList?.observe(this, Observer{ userListList ->
                listAdapter.submitList(userListList)
            })
            mListListViewModel?.loadListList()

            setUpObservers()
        })

        addListButton.setOnClickListener {
            val dialog = DialogUserListEditor.newInstance()
            dialog.show(supportFragmentManager, "")
        }
    }



    private fun setUpObservers(){
        mListListViewModel?.showUserDetailEvent?.removeObserver(showUserListDetail)
        mListListViewModel?.showUserDetailEvent?.observe(this, showUserListDetail)

        mListOperateViewModel?.updateUserListEvent?.removeObserver(showListUpdateDialogObserver)
        mListOperateViewModel?.updateUserListEvent?.observe(this, showListUpdateDialogObserver)
    }

    private val showUserListDetail = Observer<UserList>{ ul ->
        val intent = Intent(this, UserListDetailActivity::class.java)
        intent.putExtra(UserListDetailActivity.EXTRA_LIST_ID, ul.id)
        startActivityForResult(intent, USER_LIST_ACTIVITY_RESULT_CODE)
    }

    private val showListUpdateDialogObserver = Observer<UserList>{ ul ->
        val dialog = DialogUserListEditor.newInstance(ul.id, ul.name)
        dialog.show(supportFragmentManager, "")
    }
}
