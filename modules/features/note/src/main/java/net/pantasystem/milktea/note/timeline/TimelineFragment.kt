package net.pantasystem.milktea.note.timeline

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.PageableView
import net.pantasystem.milktea.common.ui.ScrollableTop
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.ScrollToTopViewModel
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentSwipeRefreshRecyclerViewBinding
import net.pantasystem.milktea.note.timeline.viewmodel.TimeMachineEventViewModel
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineViewModel
import net.pantasystem.milktea.note.timeline.viewmodel.provideViewModel
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), ScrollableTop,
    PageableView {

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

    private lateinit var mLinearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var timelineViewModelFactory: TimelineViewModel.ViewModelAssistedFactory

    private val mViewModel: TimelineViewModel by viewModels<TimelineViewModel> {
        TimelineViewModel.provideViewModel(
            timelineViewModelFactory,
            null,
            mPage?.accountId ?: accountId,
            mPageable
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


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        val adapter = TimelineListAdapter(
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
                currentPageable = mPageable
            ).onAction(it)
        }

        mBinding.listView.layoutManager = mLinearLayoutManager

        mBinding.listView.addOnScrollListener(mScrollListener)
        mBinding.listView.layoutManager = mLinearLayoutManager


        mBinding.refresh.setOnRefreshListener {
            mViewModel.loadNew()
        }

        mViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it != null && !it) {
                mBinding.refresh.isRefreshing = false
            }
        })


        mBinding.listView.adapter = adapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mViewModel.timelineListState.collect { state ->
                    adapter.submitList(state)
                }
            }

        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mViewModel.errorEvent.collect { error ->
                    TimelineErrorHandler(requireContext())(error)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                scrollToTopViewModel.scrollToTopEvent
                    .collect {
                        mBinding.listView.smoothScrollToPosition(0)
                    }
            }

        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (mViewModel.timelineStore.latestReceiveNoteId() != null && positionStart == 0 && mFirstVisibleItemPosition == 0 && isShowing && itemCount == 1) {
                    mLinearLayoutManager.scrollToPosition(0)
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
                        mViewModel.loadInit()
                        return true
                    }
                    R.id.set_time_machine -> {
                        TimeMachineDialog().show(childFragmentManager, "")
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        lifecycleScope.launch {
            whenResumed {
                timeMachineEventViewModel.loadEvents.collect {
                    mViewModel.loadInit(it)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        isShowing = true
        mViewModel.onResume()

        currentPageableTimelineViewModel.setCurrentPageable(mPageable)
        try {
            mLinearLayoutManager.scrollToPosition(mViewModel.position)
        } catch (_: Exception) {
        }

    }

    override fun onPause() {
        super.onPause()
        isShowing = false
        mViewModel.onPause()
        Log.d("TimelineFragment", "onPause")
    }


    @ExperimentalCoroutinesApi
    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            mFirstVisibleItemPosition = firstVisibleItemPosition
            mViewModel.position = firstVisibleItemPosition
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            if (endVisibleItemPosition == (itemCount - 1)) {
                mViewModel.loadOld()
            }

        }
    }

    override fun showTop() {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            mLinearLayoutManager.scrollToPosition(0)
        }
    }

}