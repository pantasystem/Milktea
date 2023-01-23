package net.pantasystem.milktea.userlist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.UserListArgs
import net.pantasystem.milktea.common_navigation.UserListNavigation
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.userlist.compose.UserListCardScreen
import net.pantasystem.milktea.userlist.compose.UserListCardScreenAction
import net.pantasystem.milktea.userlist.viewmodel.ListListViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ListListActivity : AppCompatActivity() {

    companion object{

        private const val EXTRA_ADD_USER_ID = "jp.panta.misskeyandroidclient.extra.ADD_USER_ID"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.extra.ADD_USERS_ACCOUNT_ID"

        fun newInstance(context: Context, addUserId: User.Id?): Intent {
            return Intent(context, ListListActivity::class.java).apply {
                addUserId?.let {
                    putExtra(EXTRA_ADD_USER_ID, addUserId.id)
                    putExtra(EXTRA_ACCOUNT_ID, addUserId.accountId)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    val mListListViewModel: ListListViewModel by viewModels()


    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var applyTheme: ApplyTheme


    private val addUserId: User.Id? by lazy {
        val addUserIdSt = intent.getStringExtra(EXTRA_ADD_USER_ID)
        val addUserAccountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, - 1L)
        if (addUserIdSt == null || addUserAccountId == -1L) {
            null
        } else {
            User.Id(addUserAccountId, addUserIdSt)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()


        mListListViewModel.setAddTargetUserId(addUserId)

        setContent {
            val uiState by mListListViewModel.uiState.collectAsState()
            MdcTheme {
                UserListCardScreen(uiState = uiState, onAction = { action ->
                    when(action) {
                        UserListCardScreenAction.OnNavigateUp -> {
                            finish()
                        }
                        is UserListCardScreenAction.OnUserListAddToTabToggled -> {
                            mListListViewModel.toggleTab(action.userList)
                        }
                        is UserListCardScreenAction.OnUserListCardClicked -> {
                            val intent = UserListDetailActivity.newIntent(this, action.userList.id)
                            startActivity(intent)
                        }
                        is UserListCardScreenAction.OnToggleAddUser -> {
                            mListListViewModel.toggle(action.userList, action.userId)
                        }
                        is UserListCardScreenAction.OnSaveNewUserList -> {
                            mListListViewModel.createUserList(action.name)
                        }
                    }
                })
            }
        }

    }




}

class UserListNavigationImpl @Inject constructor(
    val activity: Activity
) : UserListNavigation {
    override fun newIntent(args: UserListArgs): Intent {
        return ListListActivity.newInstance(activity, args.userId)
    }
}
