package jp.panta.misskeyandroidclient.view.tags

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import kotlinx.android.synthetic.main.fragment_sorted_hash_tag.view.*

class SortedHashTagFragment : Fragment(R.layout.fragment_sorted_hash_tag){

    companion object{
        /*const val EXTRA_SORT = "jp.panta.misskeyandroidclient.view.tags.EXTRA_SORT"
        const val EXTRA_IS_ATTACHED_TO_USER_ONLY = "jp.panta.misskeyandroidclient.view.tags.EXTRA_IS_ATTACHED_TO_USER_ONLY "
        const val EXTRA_IS_ATTACHED_TO_LOCAL_USER_ONLY = "jp.panta.misskeyandroidclient.view.tags.EXTRA_IS_ATTACHED_TO_LOCAL_USER_ONLY"
        const val EXTRA_IS_ATTACHED_TO_REMOTE_USER_ONLY = "jp.panta.misskeyandroidclient.view.tags.EXTRA_IS_ATTACHED_TO_REMOTE_USER_ONLY"*/
        const val EXTRA_HASH_TAG_CONDITION = "jp.panta.misskeyandroidclient.view.tags.EXTRA_HASH_TAG_CONDITION"
        fun newInstance(
            sort: String,
            isAttachedToUserOnly: Boolean? = null,
            isAttachedToLocalUserOnly: Boolean? = null,
            isAttachedToRemoteUserOnly: Boolean? = null
        ): SortedHashTagFragment{

            return newInstance(
                SortedHashTagListViewModel.Conditions(
                    sort = sort,
                    isAttachedToUserOnly = isAttachedToUserOnly,
                    isAttachedToLocalUserOnly = isAttachedToLocalUserOnly,
                    isAttachedToRemoteUserOnly = isAttachedToRemoteUserOnly
                )
            )
        }

        fun newInstance(conditions: SortedHashTagListViewModel.Conditions): SortedHashTagFragment{
            return SortedHashTagFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_HASH_TAG_CONDITION, conditions)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val conditions = arguments?.getSerializable(EXTRA_HASH_TAG_CONDITION) as SortedHashTagListViewModel.Conditions

        val miCore = view.context.applicationContext as MiCore
        val viewModel = ViewModelProvider(this, SortedHashTagListViewModel.Factory(miCore, conditions))[SortedHashTagListViewModel::class.java]

        val adapter = HashTagListAdapter()
        view.hashTagListView.adapter = adapter

        val flexBoxLayoutManager = FlexboxLayoutManager(view.context)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        view.hashTagListView.layoutManager = flexBoxLayoutManager
        viewModel.hashTags.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            view.hashTagListSwipeRefresh.isRefreshing = it
        })

        view.hashTagListSwipeRefresh.setOnRefreshListener {
            viewModel.load()
        }

    }
}