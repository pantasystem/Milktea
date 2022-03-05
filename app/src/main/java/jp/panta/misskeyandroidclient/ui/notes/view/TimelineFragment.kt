package jp.panta.misskeyandroidclient.ui.notes.view

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.api.misskey.APIError
import jp.panta.misskeyandroidclient.databinding.FragmentSwipeRefreshRecyclerViewBinding
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.setMenuTint
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.ui.PageableView
import jp.panta.misskeyandroidclient.ui.ScrollableTop
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.io.IOException
import java.net.SocketTimeoutException

@ExperimentalCoroutinesApi
@FlowPreview
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

    @ExperimentalCoroutinesApi
    private var mViewModel: TimelineViewModel? = null


    private var mPage: Page? = null

    private var mPageable: Pageable? = null

    /**
     * タイムラインが画面上に表示されているかを判定するフラグ
     */
    private var isShowing: Boolean = false

    private var mFirstVisibleItemPosition: Int? = null

    private lateinit var sharedPreference: SharedPreferences

    lateinit var miApplication: MiApplication

    val mBinding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()
    
    val notesViewModel by activityViewModels<NotesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPage = arguments?.getSerializable(EXTRA_PAGE) as? Page

        mPageable = arguments?.getSerializable(EXTRA_PAGEABLE) as? Pageable

        sharedPreference = requireContext().getSharedPreferences(
            requireContext().getPreferenceName(),
            MODE_PRIVATE
        )

        miApplication = context?.applicationContext as MiApplication



        Log.d("TimelineFM", "page:${mPage?.pageable() ?: mPageable}")
        val pageable = mPage?.pageable() ?: mPageable
        ?: throw IllegalStateException("構築に必要な情報=Pageableがありません。")
        val factory = TimelineViewModelFactory(null, mPage?.accountId, pageable, miApplication)
        mViewModel = ViewModelProvider(this, factory).get(TimelineViewModel::class.java)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        
        //sharedPreference = view.context.getSharedPreferences()

        mLinearLayoutManager = LinearLayoutManager(this.requireContext())
        mBinding.listView.layoutManager = mLinearLayoutManager

        mBinding.listView.addOnScrollListener(mScrollListener)
        mBinding.listView.layoutManager = mLinearLayoutManager


        mBinding.refresh.setOnRefreshListener {
            mViewModel?.loadNew()
        }

        mViewModel?.isLoading?.observe(viewLifecycleOwner) {
            if (it != null && !it) {
                mBinding.refresh.isRefreshing = false
            }
        }

        //mLinearLayoutManager.scrollToPosition(mViewModel?.position?.value?: 0)
        val adapter = TimelineListAdapter(diffUtilCallBack, viewLifecycleOwner, notesViewModel)
        mBinding.listView.adapter = adapter

        var timelineState: TimelineState? = null
        lifecycleScope.launchWhenResumed {
            mViewModel?.getTimelineState()?.collect { tm ->
                adapter.submitList(tm.notes)
                timelineState = tm

                if (tm.notes.isNullOrEmpty()) {
                    mBinding.timelineEmptyView.visibility = View.VISIBLE
                    mBinding.refresh.visibility = View.GONE
                } else {
                    mBinding.timelineEmptyView.visibility = View.GONE
                    mBinding.refresh.visibility = View.VISIBLE
                }
                mBinding.timelineProgressBar.visibility = View.GONE
            }
        }


        mViewModel?.isInitLoading?.observe(viewLifecycleOwner) {
            if (it) {

                mBinding.timelineProgressBar.visibility = View.VISIBLE
                mBinding.refresh.visibility = View.GONE
                mBinding.timelineEmptyView.visibility = View.GONE
            } else {
                mBinding.timelineProgressBar.visibility = View.GONE

            }
        }


        lifecycleScope.launchWhenResumed {
            mViewModel?.errorEvent?.collect { error ->
                when (error) {
                    is IOException -> {
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_LONG)
                            .show()

                    }
                    is SocketTimeoutException -> {
                        Toast.makeText(requireContext(), R.string.timeout_error, Toast.LENGTH_LONG)
                            .show()

                    }
                    is APIError.AuthenticationException -> {
                        Toast.makeText(requireContext(), R.string.auth_error, Toast.LENGTH_LONG)
                            .show()
                    }
                    is APIError.IAmAIException -> {
                        Toast.makeText(requireContext(), R.string.bot_error, Toast.LENGTH_LONG)
                            .show()
                    }
                    is APIError.InternalServerException -> {
                        Toast.makeText(requireContext(), R.string.auth_error, Toast.LENGTH_LONG)
                            .show()
                    }
                    is APIError.ClientException -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.parameter_error,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "error:$error", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if (timelineState is TimelineState.ReceivedNew && positionStart == 0 && mFirstVisibleItemPosition == 0 && isShowing && itemCount == 1) {
                    mLinearLayoutManager.scrollToPosition(0)
                }

            }
        })

        mViewModel?.position?.let {
            try {
                mLinearLayoutManager.scrollToPosition(it)
            } catch (e: Exception) {

            }
        }

        mBinding.retryLoadButton.setOnClickListener {
            Log.d("TimelineFragment", "リトライボタンを押しました")
            mViewModel?.loadInit()
        }

        //mViewModel?.loadInit()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        mFirstVisibleItemPosition?.let { firstVisibleItemPosition ->
            if (firstVisibleItemPosition > 0 && firstVisibleItemPosition <= mViewModel?.getTimelineState()?.value?.notes?.size ?: 0) {
                try {
                    mViewModel?.getTimelineState()?.value?.notes?.get(firstVisibleItemPosition)
                } catch (t: Throwable) {
                    Log.e("TimelineFragment", "先端に表示されているノートを取得しようとしたら失敗した", t)
                    null
                }
            } else {
                null
            }
        }

    }

    override fun onResume() {
        super.onResume()

        isShowing = true


        //(activity as MainActivity).changeTitle(TabFragment.localizationTitle(mSetting!!))
    }

    override fun onPause() {
        super.onPause()

        isShowing = false
        Log.d("TimelineFragment", "onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //mViewModel?.streamingStop()
        //mViewModel?.stop()
        Log.d("TimelineFragment", "onDestroyView")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_timeline, menu)
        requireContext().setMenuTint(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh_timeline -> {
                mViewModel?.loadInit()
            }
        }
        return super.onOptionsItemSelected(item)
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
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            mFirstVisibleItemPosition = firstVisibleItemPosition
            mViewModel?.position = firstVisibleItemPosition

            if (firstVisibleItemPosition == 0) {
                Log.d("", "先頭")
            }

            if (endVisibleItemPosition == (itemCount - 1)) {
                Log.d("", "後ろ")
                mViewModel?.loadOld()

            }

        }
    }

    override fun showTop() {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            mLinearLayoutManager.scrollToPosition(0)
        }
    }

}