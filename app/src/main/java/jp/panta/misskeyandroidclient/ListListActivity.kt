package jp.panta.misskeyandroidclient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.databinding.ActivityListListBinding
import jp.panta.misskeyandroidclient.ui.list.ListListAdapter
import jp.panta.misskeyandroidclient.ui.list.UserListEditorDialog
import jp.panta.misskeyandroidclient.ui.list.viewmodel.ListListViewModel
import jp.panta.misskeyandroidclient.ui.list.viewmodel.UserListPullPushUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_navigation.UserListNavigation
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@AndroidEntryPoint
class ListListActivity : AppCompatActivity(), ListListAdapter.OnTryToEditCallback, UserListEditorDialog.OnSubmittedListener{

    companion object{

        private const val EXTRA_ADD_USER_ID = "jp.panta.misskeyandroidclient.extra.ADD_USER_ID"

        fun newInstance(context: Context, addUserId: User.Id?): Intent {
            return Intent(context, ListListActivity::class.java).apply {
                addUserId?.let {
                    putExtra(EXTRA_ADD_USER_ID, addUserId)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    val mListListViewModel: ListListViewModel by viewModels()

    private val pullPushUserViewModel: UserListPullPushUserViewModel by viewModels()

    @Inject
    lateinit var accountStore: AccountStore



    private lateinit var mBinding: ActivityListListBinding

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list_list)

        val addUserId = intent.getSerializableExtra(EXTRA_ADD_USER_ID) as? User.Id

        val layoutManager = LinearLayoutManager(this)

        val listAdapter =
        if(addUserId == null){
            ListListAdapter(
                mListListViewModel,
                this,
                this
            )
        }else{

            accountStore.observeCurrentAccount.filterNotNull().onEach{
                pullPushUserViewModel.account.value = it
            }.launchIn(lifecycleScope)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    mListListViewModel.fetch()
                }
            }

            ListListAdapter(
                mListListViewModel,
                this,
                this,
                addUserId,
                pullPushUserViewModel
            )
        }


        mBinding.contentListList.listListView.adapter = listAdapter
        mBinding.contentListList.listListView.layoutManager = layoutManager
        mListListViewModel.userListList.observe(this) { userListList ->
            listAdapter.submitList(userListList)
        }


        setUpObservers()
        mBinding.addListButton.setOnClickListener {
            val dialog = UserListEditorDialog.newInstance()
            dialog.show(supportFragmentManager, "")
        }
    }



    @ExperimentalCoroutinesApi
    private fun setUpObservers(){
        mListListViewModel.showUserDetailEvent.removeObserver(showUserListDetail)
        mListListViewModel.showUserDetailEvent.observe(this, showUserListDetail)

    }

    @OptIn(FlowPreview::class)
    @ExperimentalCoroutinesApi
    private val showUserListDetail = Observer<UserList>{ ul ->
        val intent = UserListDetailActivity.newIntent(this, ul.id)
        startActivity(intent)
    }



    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onEdit(userList: UserList?) {
        userList?: return

        val intent = UserListDetailActivity.newIntent(this, userList.id)
        intent.action = UserListDetailActivity.ACTION_EDIT_NAME
        startActivity(intent)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onSubmit(name: String) {
        mListListViewModel.createUserList(name)
    }



}

class UserListNavigationImpl @Inject constructor(
    val activity: Activity
) : UserListNavigation {
    override fun newIntent(args: Unit): Intent {
        return Intent(activity, ListListActivity::class.java)
    }
}
