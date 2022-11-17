@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.userlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_compose.CustomEmojiText
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
import net.pantasystem.milktea.common_viewmodel.confirm.ConfirmViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.userlist.viewmodel.UserListDetailViewModel
import javax.inject.Inject

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

    private var mListId: UserList.Id? = null

    @Inject
    lateinit var assistedFactory: UserListDetailViewModel.ViewModelAssistedFactory

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    lateinit var searchAndSelectUserNavigation: SearchAndSelectUserNavigation

    @Inject
    lateinit var userDetailPageNavigation: UserDetailNavigation

    private val mUserListDetailViewModel: UserListDetailViewModel by viewModels {
        val listId = intent.getSerializableExtra(EXTRA_LIST_ID) as UserList.Id
        UserListDetailViewModel.provideFactory(assistedFactory, listId)
    }

    private var mUserListName: String = ""

    //    private val binding: ActivityUserListDetailBinding by dataBinding()
    val notesViewModel by viewModels<NotesViewModel>()

    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var applyMenuTint: ApplyMenuTint

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_user_list_detail)

        val listId = intent.getSerializableExtra(EXTRA_LIST_ID) as UserList.Id

        mListId = listId


        net.pantasystem.milktea.note.view.ActionNoteHandler(
            this,
            notesViewModel,
            ViewModelProvider(this)[ConfirmViewModel::class.java],
            settingStore
        ).initViewModelListener()

        setContent {
            MdcTheme {
                val userList by mUserListDetailViewModel.userList.collectAsState()
                val titles = listOf(stringResource(R.string.timeline), stringResource(R.string.user_list))
                val pagerState = rememberPagerState(pageCount = titles.size)
                val users by mUserListDetailViewModel.users.collectAsState()
                val isAddedTab by mUserListDetailViewModel.isAddedToTab.collectAsState()

                val scope = rememberCoroutineScope()
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                                    }
                                },
                                title = {
                                    Text(userList?.name ?: "")
                                },
                                actions = {
                                    IconButton(onClick = {
                                        val selected =
                                            mUserListDetailViewModel.users.value.map {
                                                it.id
                                            }
                                        val intent = searchAndSelectUserNavigation.newIntent(
                                            SearchAndSelectUserNavigationArgs(
                                                selectedUserIds = selected
                                            )
                                        )
                                        requestSelectUserResult.launch(intent)
                                    }) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    }

                                    IconButton(onClick = {
                                        showEditUserListDialog()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = null)
                                    }
                                    IconButton(onClick = {
                                        mUserListDetailViewModel.toggleAddToTab()
                                    }) {
                                        if(isAddedTab) {
                                            Icon(Icons.Default.BookmarkRemove, contentDescription = null)
                                        } else {
                                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                                        }
                                    }

                                }

                            )
                            TabRow(selectedTabIndex = pagerState.currentPage) {
                                titles.forEachIndexed { index, s ->
                                    Tab(
                                        text = { Text(text = s) },
                                        selected = index == pagerState.currentPage,
                                        onClick = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                    )
                                }

                            }
                        }
                    }
                ) {
                    HorizontalPager(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize(),
                        state = pagerState,
                    ) {
                        when (pagerState.currentPage) {
                            0 -> {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { context ->
                                        FrameLayout(context).apply {
                                            id = R.id.container
                                        }
                                    },
                                    update = { frameLayout ->
                                        val fragment = pageableFragmentFactory.create(
                                            Pageable.UserListTimeline(listId = listId.userListId)
                                        )
                                        val transaction = supportFragmentManager.beginTransaction()
                                        transaction.replace(frameLayout.id, fragment)
                                        transaction.commit()
                                    }
                                )
                            }
                            1 -> {
                                LazyColumn(Modifier.fillMaxSize()) {
                                    items(users) { user ->
                                        ItemSimpleUserCard(
                                            user = user,
                                            onSelected = { u ->
                                                startActivity(userDetailPageNavigation.newIntent(
                                                    UserDetailNavigationArgs.UserId(u.id)
                                                ))
                                            },
                                            onDeleteButtonClicked = { u ->
                                                mUserListDetailViewModel.pullUser(u.id)
                                            }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
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
        val listId = mListId ?: return
        val dialog = UserListEditorDialog.newInstance(listId.userListId, mUserListName)
        dialog.show(supportFragmentManager, "")
    }


}




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ItemSimpleUserCard(
    user: User,
    onSelected: (User) -> Unit,
    onDeleteButtonClicked: (User) -> Unit,
) {

    Card(
        onClick = {
            onSelected.invoke(user)
        },
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .weight(1f),
            ) {
                Image(
                    painter = rememberAsyncImagePainter(user.avatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    CustomEmojiText(text = user.displayName, emojis = user.emojis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = user.displayUserName)
                }
            }
            IconButton(
                onClick = {
                    onDeleteButtonClicked(user)
                },

            ) {

                Icon(Icons.Default.Delete ,contentDescription = null)
            }
        }

    }
}
