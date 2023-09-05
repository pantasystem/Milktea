package net.pantasystem.milktea.note.timeline

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.PageableView
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.ScrollToTopViewModel
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentSwipeRefreshRecyclerViewBinding
import net.pantasystem.milktea.note.timeline.viewmodel.*
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), PageableView {

    companion object {

        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclient.EXTRA_PAGE"
        private const val EXTRA_PAGEABLE = "jp.panta.misskeyandroidclient.EXTRA_PAGEABLE"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"

        fun newInstance(page: Page): TimelineFragment {
            return TimelineFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_PAGE, page)
                }
            }
        }

        fun newInstance(pageable: Pageable, accountId: Long? = null): TimelineFragment {
            return TimelineFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_PAGEABLE, pageable)
                    if (accountId != null) {
                        putLong(EXTRA_ACCOUNT_ID, accountId)
                    }
                }
            }
        }

    }

    private var _linearLayoutManager: LinearLayoutManager? = null
    private val layoutManager: LinearLayoutManager
        get() = requireNotNull(_linearLayoutManager)

    @Inject
    internal lateinit var timelineViewModelFactory: TimelineViewModel.ViewModelAssistedFactory

    private val mViewModel: TimelineViewModel by viewModels<TimelineViewModel> {
        TimelineViewModel.provideViewModel(
            timelineViewModelFactory,
            accountId = (mPage?.attachedAccountId?: mPage?.accountId ?: accountId)?.let {
                AccountId(it)
            },
            pageId = mPage?.pageId?.let {
                PageId(it)
            },
            pageable = mPageable,
            isSaveScrollPosition = mPage?.isSavePagePosition
        )
    }

    private val timeMachineEventViewModel by activityViewModels<TimeMachineEventViewModel>()

    private val scrollToTopViewModel by activityViewModels<ScrollToTopViewModel>()

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var setMenuTint: ApplyMenuTint

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var channelDetailNavigation: ChannelDetailNavigation

    @Inject
    lateinit var configRepository: LocalConfigRepository


    private val mBinding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()

    @Suppress("DEPRECATION")
    private val mPage: Page? by lazy {
        arguments?.getSerializable(EXTRA_PAGE) as? Page
    }

    private val accountId: Long? by lazy {
        arguments?.getLong(EXTRA_ACCOUNT_ID, -1).takeIf {
            it != -1L
        }
    }

    @Suppress("DEPRECATION")
    private val mPageable: Pageable by lazy {
        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as? Pageable
        mPage?.pageable() ?: pageable ?: throw IllegalStateException("構築に必要な情報=Pageableがありません。")
    }

    /**
     * タイムラインが画面上に表示されているかを判定するフラグ
     */
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null


    val notesViewModel by activityViewModels<NotesViewModel>()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lm = LinearLayoutManager(this.requireContext())
        _linearLayoutManager = lm
        val adapter = TimelineListAdapter(
            configRepository = configRepository,
            viewLifecycleOwner,
            onRefreshAction = {
                mViewModel.loadInit()
            },
            onReauthenticateAction = {
                startActivity(
                    authorizationNavigation.newIntent(
                        AuthorizationArgs.ReAuth(
                            accountStore.currentAccount
                        )
                    )
                )
            },
        ) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore,
                userDetailNavigation,
                channelDetailNavigation,
                currentPageable = mPageable,
            ).onAction(it)
        }

        adapter.AvatarIconPreloadProvider(requireContext()).setup(mBinding.listView)

        mBinding.listView.layoutManager = lm

        mBinding.refresh.setOnRefreshListener {
            mViewModel.loadNew()
        }

        mViewModel.isLoading.onEach {
            if (!it) {
                mBinding.refresh.isRefreshing = false
            }
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)


        mBinding.listView.adapter = adapter
        mBinding.listView.setItemViewCacheSize(8)

        mViewModel.timelineListState.onEach { state ->
            adapter.submitList(state)
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)


        mViewModel.errorEvent.onEach { error ->
            TimelineErrorHandler(requireContext())(error)
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)


        scrollToTopViewModel.scrollToTopEvent.onEach {
                mBinding.listView.smoothScrollToPosition(0)
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)


        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (mViewModel.timelineStore.latestReceiveNoteId() != null && positionStart == 0 && mFirstVisibleItemPosition == 0 && isShowing && itemCount == 1) {
                    lm.scrollToPosition(0)
                }
            }
        })


        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_timeline, menu)
                setMenuTint(requireActivity(), menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.refresh_timeline -> {
                        mViewModel.loadInit(ignoreSavedScrollPosition = true)
                        return true
                    }
                    R.id.set_time_machine -> {
                        TimeMachineDialog().show(childFragmentManager, TimeMachineDialog.FRAGMENT_TAG)
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            whenResumed {
                timeMachineEventViewModel.loadEvents.collect {
                    mViewModel.loadInit(it, ignoreSavedScrollPosition = true)
                }
            }
        }

        mBinding.listView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()
                    mFirstVisibleItemPosition = firstVisibleItemPosition
                    mViewModel.position = firstVisibleItemPosition
                    val topView = layoutManager.findViewByPosition(firstVisibleItemPosition)
                    if (topView != null) {
                        val top = topView.top - topView.paddingTop
                        mViewModel.offset = top
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    val endVisibleItemPosition = lm.findLastVisibleItemPosition()
                    val itemCount = lm.itemCount

                    if (endVisibleItemPosition == (itemCount - 1)) {
                        mViewModel.loadOld()
                    }

                    mViewModel.onScrollPositionChanged(lm.findFirstVisibleItemPosition())

                }
            }
        )
    }


    override fun onResume() {
        super.onResume()

        isShowing = true
        mViewModel.onResume()

        currentPageableTimelineViewModel.setCurrentPageable(mViewModel.accountId?.value, mPageable)
        try {
            layoutManager.scrollToPositionWithOffset(mViewModel.position, mViewModel.offset)
        } catch (_: Exception) {
        }

    }

    override fun onPause() {
        super.onPause()
        isShowing = false
        mViewModel.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _linearLayoutManager = null
    }

}