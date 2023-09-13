@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.userlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_compose.MilkteaStyleConfigApplyAndTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.view.ActionNoteHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.userlist.compose.UserListDetailScreen
import net.pantasystem.milktea.userlist.viewmodel.UserListDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserListDetailActivity : AppCompatActivity(), UserListEditorDialog.OnSubmittedListener {

    companion object {
        private const val TAG = "UserListDetailActivity"


        const val ACTION_SHOW = "ACTION_SHOW"
        const val ACTION_EDIT_NAME = "ACTION_EDIT_NAME"


        fun newIntent(context: Context, listId: UserList.Id, addTabToAccountId: Long? = null): Intent {
            return Intent(context, UserListDetailActivity::class.java).apply {
                putExtra(UserListDetailViewModel.EXTRA_LIST_ID, listId)
                putExtra(UserListDetailViewModel.EXTRA_ADD_TAB_TO_ACCOUNT_ID, addTabToAccountId)
            }
        }
    }



    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    lateinit var searchAndSelectUserNavigation: SearchAndSelectUserNavigation

    @Inject
    lateinit var userDetailPageNavigation: UserDetailNavigation

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var applyMenuTint: ApplyMenuTint

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

    private val mUserListDetailViewModel: UserListDetailViewModel by viewModels()

    private val notesViewModel by viewModels<NotesViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        ActionNoteHandler(
            this.supportFragmentManager,
            this,
            this,
            notesViewModel,
        ).initViewModelListener()

        setContent {
            MilkteaStyleConfigApplyAndTheme(configRepository = configRepository) {
                val userList by mUserListDetailViewModel.userList.collectAsState()

                val users by mUserListDetailViewModel.users.collectAsState()
                val isAddedTab by mUserListDetailViewModel.isAddedToTab.collectAsState()
                val account by mUserListDetailViewModel.account.collectAsState()

                UserListDetailScreen(
                    listId = mUserListDetailViewModel.getUserListId(),
                    userList = userList?.userList,
                    users = users,
                    isAddedTab = isAddedTab,
                    onNavigateUp = {
                        finish()
                    },
                    fragmentManager = supportFragmentManager,
                    pageableFragmentFactory = pageableFragmentFactory,
                    accountHost = account?.getHost(),
                    onToggleButtonClicked = {
                        mUserListDetailViewModel.toggleAddToTab()
                    },
                    onEditButtonClicked = {
                        showEditUserListDialog()
                    },
                    onAddUserButtonClicked = {
                        val selected =
                            mUserListDetailViewModel.users.value.map {
                                it.id
                            }
                        val intent = searchAndSelectUserNavigation.newIntent(
                            SearchAndSelectUserNavigationArgs(
                                selectedUserIds = selected,
                                accountId = mUserListDetailViewModel.getUserListId().accountId
                            )
                        )
                        requestSelectUserResult.launch(intent)
                    },
                    onSelectUser = {
                        startActivity(
                            userDetailPageNavigation.newIntent(
                                UserDetailNavigationArgs.UserId(it.id)
                            )
                        )
                    },
                    onDeleteUserButtonClicked = {
                        mUserListDetailViewModel.pullUser(it.id)
                    },
                    instanceType = account?.instanceType
                )

            }
        }



        if (intent.action == ACTION_EDIT_NAME) {
            intent.action = ACTION_SHOW
            showEditUserListDialog()
        }

    }

    override fun onSubmit(name: String) {
        mUserListDetailViewModel.updateName(name)
    }


    private val requestSelectUserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == RESULT_OK) {
                val changedDiff =
                    data?.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as? ChangedDiffResult
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
        val dialog = UserListEditorDialog.newInstance(
            mUserListDetailViewModel.getUserListId().userListId,
            mUserListDetailViewModel.userList.value?.userList?.name ?: ""
        )
        dialog.show(supportFragmentManager, UserListEditorDialog.FRAGMENT_TAG)
    }


}

