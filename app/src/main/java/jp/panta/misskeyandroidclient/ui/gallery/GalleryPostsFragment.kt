package jp.panta.misskeyandroidclient.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.AuthorizationActivity
import jp.panta.misskeyandroidclient.R
import net.pantasystem.milktea.data.api.misskey.APIError
import jp.panta.misskeyandroidclient.databinding.FragmentSwipeRefreshRecyclerViewBinding
import net.pantasystem.milktea.data.model.account.page.Pageable
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryPostsViewModel
import jp.panta.misskeyandroidclient.viewmodel.timeline.CurrentPageableTimelineViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class GalleryPostsFragment : Fragment(R.layout.fragment_swipe_refresh_recycler_view){

    companion object {
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.view.gallery.ACCOUNT_ID"
        private const val EXTRA_PAGEABLE = "jp.panta.misskeyandroidclient.view.gallery.EXTRA_PAGEABLE"

        fun newInstance(pageable: Pageable.Gallery, accountId: Long?) : GalleryPostsFragment {
            return GalleryPostsFragment().apply {
                arguments = Bundle().also {
                    it.putSerializable(EXTRA_PAGEABLE, pageable)
                    if(accountId != null) {
                        it.putLong(EXTRA_ACCOUNT_ID, accountId)
                    }
                }
            }
        }
    }

    val binding: FragmentSwipeRefreshRecyclerViewBinding by dataBinding()

    val pageable: Pageable.Gallery by lazy {
        arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
    }

    val currentTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
        var accountId = arguments?.getLong(EXTRA_ACCOUNT_ID, -1)
        if(accountId == -1L) {
            accountId = null
        }



        val miCore = requireContext().applicationContext as MiCore
        val viewModel = ViewModelProvider(this, GalleryPostsViewModel.Factory(pageable, accountId, miCore))[GalleryPostsViewModel::class.java]

        val galleryPostsListAdapter = GalleryPostsListAdapter(viewLifecycleOwner, viewModel)

        binding.listView.adapter = galleryPostsListAdapter
        val layoutManager = LinearLayoutManager(this.context)
        binding.listView.layoutManager = layoutManager
        lifecycleScope.launchWhenStarted {
            viewModel.galleryPosts.collect { state ->
                if(state.content is net.pantasystem.milktea.common.StateContent.Exist) {
                    // 要素を表示する
                    binding.refresh.visibility = View.VISIBLE
                    binding.timelineEmptyView.visibility = View.GONE
                    binding.timelineProgressBar.visibility = View.GONE
                    galleryPostsListAdapter.submitList(state.content.rawContent)
                    binding.refresh.isRefreshing = state is net.pantasystem.milktea.common.PageableState.Loading
                }else{
                    // エラーメッセージやプログレスバーなどを表示する
                    binding.refresh.isRefreshing = false
                    binding.refresh.visibility = View.GONE
                    if(state is net.pantasystem.milktea.common.PageableState.Loading) {
                        binding.timelineProgressBar.visibility = View.VISIBLE
                        binding.timelineEmptyView.visibility = View.GONE
                    }else{
                        binding.timelineProgressBar.visibility = View.GONE
                        binding.timelineEmptyView.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.refresh.setOnRefreshListener {
            viewModel.loadFuture()
        }
        binding.retryLoadButton.setOnClickListener {
            viewModel.loadInit()
        }

        lifecycleScope.launchWhenResumed {
            viewModel.error.collect {
                if(it is APIError.ClientException && it.error?.error?.code == "PERMISSION_DENIED") {
                    Toast.makeText(requireContext(), "再認証が必要です。", Toast.LENGTH_LONG).show()
                    // 再認証をする
                    startActivity(Intent(requireContext(), AuthorizationActivity::class.java))
                }
            }
        }

        binding.listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if(layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1){
                    viewModel.loadPrevious()
                }
            }

        })
    }


    override fun onResume() {
        super.onResume()
        currentTimelineViewModel.setCurrentPageable(pageable)
    }

}