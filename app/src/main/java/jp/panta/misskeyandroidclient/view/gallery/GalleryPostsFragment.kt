package jp.panta.misskeyandroidclient.view.gallery

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentSwipeRefreshRecyclerViewBinding
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.gallery.GalleryPostsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageable = arguments?.getSerializable(EXTRA_PAGEABLE) as Pageable.Gallery
        val accountId = arguments?.getLong(EXTRA_ACCOUNT_ID)

        val galleryPostsListAdapter = GalleryPostsListAdapter(viewLifecycleOwner)
        binding.listView.adapter = galleryPostsListAdapter
        val layoutManager = LinearLayoutManager(this.context)
        binding.listView.layoutManager = layoutManager

        val miCore = requireContext().applicationContext as MiCore
        val viewModel = ViewModelProvider(this, GalleryPostsViewModel.Factory(pageable, accountId, miCore))[GalleryPostsViewModel::class.java]
        viewModel.galleryPosts.onEach { state ->
            if(state.content is StateContent.Exist) {
                // 要素を表示する
                binding.refresh.visibility = View.VISIBLE
                binding.timelineEmptyView.visibility = View.GONE
                binding.timelineProgressBar.visibility = View.GONE
                galleryPostsListAdapter.submitList(state.content.rawContent)
                binding.refresh.isRefreshing = state is State.Loading
            }else{
                // エラーメッセージやプログレスバーなどを表示する
                binding.refresh.isRefreshing = false
                binding.refresh.visibility = View.GONE
                if(state is State.Loading) {
                    binding.timelineProgressBar.visibility = View.VISIBLE
                    binding.timelineEmptyView.visibility = View.GONE
                }else{
                    binding.timelineProgressBar.visibility = View.GONE
                    binding.timelineEmptyView.visibility = View.VISIBLE
                }
            }
        }.launchIn(lifecycleScope)

        binding.refresh.setOnRefreshListener {
            viewModel.loadFuture()
        }
        binding.retryLoadButton.setOnClickListener {
            viewModel.loadInit()
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

}