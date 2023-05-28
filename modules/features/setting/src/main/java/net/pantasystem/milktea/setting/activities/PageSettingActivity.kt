package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.EditTabSettingDialog
import net.pantasystem.milktea.setting.PageSettingActionDialog
import net.pantasystem.milktea.setting.compose.tab.TabItemsListScreen
import net.pantasystem.milktea.setting.compose.tab.rememberDragDropListState
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PageSettingActivity : AppCompatActivity() {


    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var searchAndSelectUserNavigation: SearchAndSelectUserNavigation

    @Inject
    lateinit var searchNavigation: SearchNavigation

    @Inject
    lateinit var antennaNavigation: AntennaNavigation

    @Inject
    lateinit var channelNavigation: ChannelNavigation

    @Inject
    lateinit var userListNavigation: UserListNavigation

    @Inject
    lateinit var clipListNavigation: ClipListNavigation

    private val mPageSettingViewModel: PageSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        mPageSettingViewModel.pageOnActionEvent.observe(this) {
            PageSettingActionDialog().show(supportFragmentManager, "PSA")
        }

        mPageSettingViewModel.pageOnUpdateEvent.observe(this) {
            EditTabSettingDialog().show(supportFragmentManager, "ETD")
        }
//
        mPageSettingViewModel.pageAddedEvent.observe(this) { pt ->
            when (pt) {
                PageType.SEARCH, PageType.SEARCH_HASH, PageType.MASTODON_HASHTAG_TIMELINE -> startActivity(
                    searchNavigation.newIntent(SearchNavType.SearchScreen())
                )
                PageType.USER -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(
                            SearchAndSelectUserNavigationArgs(
                                selectableMaximumSize = 1
                            )
                        )
                    launchSearchAndSelectUserForAddUserTimelineTab.launch(intent)
                }
                PageType.USER_LIST, PageType.MASTODON_LIST_TIMELINE -> startActivity(
                    userListNavigation.newIntent(UserListArgs())
                )
                PageType.DETAIL -> startActivity(searchNavigation.newIntent(SearchNavType.SearchScreen()))
                PageType.ANTENNA -> startActivity(antennaNavigation.newIntent(Unit))
                PageType.CLIP_NOTES -> startActivity(
                    clipListNavigation.newIntent(
                        ClipListNavigationArgs(mode = ClipListNavigationArgs.Mode.AddToTab)
                    )
                )
                PageType.USERS_GALLERY_POSTS -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(
                            SearchAndSelectUserNavigationArgs(
                                selectableMaximumSize = 1
                            )
                        )
                    launchSearchAndSelectUserForAddGalleryTab.launch(intent)
                }
                PageType.CHANNEL_TIMELINE -> {
                    val intent = channelNavigation.newIntent(Unit)
                    startActivity(intent)
                }
                else -> {
                    // auto add
                }
            }
        }


        setContent {
            MdcTheme {
                val pageTypes by mPageSettingViewModel.pageTypesGroupedByAccount.collectAsState()
                val list by mPageSettingViewModel.selectedPages.collectAsState()
                val scope = rememberCoroutineScope()
                val dragAndDropState =
                    rememberDragDropListState(scope = scope, onMove = { from, to ->
                        val tmp = list[to]
                        val mutable = list.toMutableList()
                        mutable[to] = mutable[from]
                        mutable[from] = tmp
                        mPageSettingViewModel.setList(mutable)
                    })

                TabItemsListScreen(
                    pageTypes = pageTypes,
                    list = list,
                    onSelectPage = {
                        mPageSettingViewModel.add(it.type)
                    },
                    onOptionButtonClicked = {
                        mPageSettingViewModel.pageOnActionEvent.event = it
                    },
                    onNavigateUp = {
                        finish()
                    },
                    dragDropState = dragAndDropState,
                )
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mPageSettingViewModel.save()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private val launchSearchAndSelectUserForAddGalleryTab =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val changeDiff =
                    it.data!!.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as ChangedDiffResult
                mPageSettingViewModel.addUsersGalleryByIds(changeDiff.selected)
            }
        }

    private val launchSearchAndSelectUserForAddUserTimelineTab =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val changeDiff =
                    it.data!!.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as ChangedDiffResult
                mPageSettingViewModel.addUserPageByIds(changeDiff.selected)
            }
        }


}
