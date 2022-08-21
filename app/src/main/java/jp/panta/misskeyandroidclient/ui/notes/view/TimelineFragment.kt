package jp.panta.misskeyandroidclient.ui.notes.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentSwipeRefreshRecyclerViewBinding
import jp.panta.misskeyandroidclient.setMenuTint
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.TimelineViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.provideViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.PageableView
import net.pantasystem.milktea.common.ui.ScrollableTop
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class TimelineFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view), ScrollableTop,
    PageableView {

    companion object {

        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclient.EXTRA_PAGE"
        private const val EXTRA_PAGEABLE = "jp.panta.misskeyandroidclient.EXTRA_PAGEABLE"

        fun newInstance(page: Page): TimelineFragment {
            return TimelineFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_PAGE, page)
                }
            }
        }

        fun newInstance(pageable: Pageable): TimelineFragment {
            return TimelineFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_PAGEABLE, pageable)
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
            mPage?.accountId,
            mPageable
        )
    }

    @Inject
    lateinit var settingStore: SettingStore

    private val mBinding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()

    private val mPage: Page? by lazy {
        arguments?.getSerializable(EXTRA_PAGE) as? Page
    }

    private val mPageable: Pageable by lazy {
        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as? Pageable
        mPage?.pageable() ?: pageable ?: throw IllegalStateException("構築に必要な情報=Pageableがありません。")
    }

    /**
     * タイムラインが画面上に表示されているかを判定するフラグ
     */
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null


//    val mBinding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()

    val notesViewModel by activityViewModels<NotesViewModel>()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore
            ).onAction(it)
        }

        mBinding.listView.layoutManager = mLinearLayoutManager

        mBinding.listView.addOnScrollListener(mScrollListener)
        mBinding.listView.layoutManager = mLinearLayoutManager


        mBinding.refresh.setOnRefreshListener {
            mViewModel.loadNew()
        }

        mViewModel.isLoading.observe(viewLifecycleOwner) {
            if (it != null && !it) {
                mBinding.refresh.isRefreshing = false
            }
        }


        mBinding.listView.adapter = adapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            mViewModel.timelineState.collect { state ->
                val notes = (state.content as? StateContent.Exist)?.rawContent ?: emptyList()
                adapter.submitList(notes)

                mBinding.timelineProgressBar.isVisible =
                    state is PageableState.Loading && state.content is StateContent.NotExist

                mBinding.refresh.isVisible = state.content is StateContent.Exist
                when (state.content) {
                    is StateContent.Exist -> {
                        mBinding.timelineEmptyView.visibility = View.GONE
                    }
                    is StateContent.NotExist -> {
                        mBinding.timelineEmptyView.isVisible = state is PageableState.Error
                    }
                }
            }
            }

        }

        mBinding.retryLoadButton.setOnClickListener {
            Log.d("TimelineFragment", "リトライボタンを押しました")
            mViewModel.loadInit()
        }


        lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mViewModel.errorEvent.collect { error ->
                    TimelineErrorHandler(requireContext())(error)
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

        mViewModel.position.let {
            try {
                mLinearLayoutManager.scrollToPosition(it)
            } catch (e: Exception) {

            }
        }



        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_timeline, menu)
                requireActivity().setMenuTint(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.refresh_timeline -> {
                        mViewModel.loadInit()
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onResume() {
        super.onResume()

        isShowing = true
        currentPageableTimelineViewModel.setCurrentPageable(mPageable)

    }

    override fun onPause() {
        super.onPause()

        isShowing = false
        Log.d("TimelineFragment", "onPause")
    }


    private val diffUtilCallBack = object : DiffUtil.ItemCallback<PlaneNoteViewData>() {
        override fun areContentsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(
            oldItem: PlaneNoteViewData,
            newItem: PlaneNoteViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    @ExperimentalCoroutinesApi
    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (settingStore.configState.value.isEnableTimelineScrollAnimation) {
                val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
                val vh = recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition)
                val firstVisibleVH = vh as? TimelineListAdapter.NoteViewHolderBase<*>
                if (firstVisibleVH != null) {
                    val icon = firstVisibleVH.getAvatarIcon()
                    val parent = icon.parent as ViewGroup
                    val y = (abs(vh.itemView.top) + icon.marginTop + parent.paddingTop).toFloat()
                    if ((y + icon.height) <= (parent.height)) {
                        icon.y = y
                    }
                }
            }

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            mFirstVisibleItemPosition = firstVisibleItemPosition
            mViewModel.position = firstVisibleItemPosition

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            if (endVisibleItemPosition == (itemCount - 1)) {
                Log.d("", "後ろ")
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