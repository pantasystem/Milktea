package net.pantasystem.milktea.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.search.databinding.FragmentSearchTopBinding
import net.pantasystem.milktea.search.explore.ExploreFragment
import net.pantasystem.milktea.search.explore.ExploreType
import net.pantasystem.milktea.search.trend.TrendFragment
import javax.inject.Inject


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SearchTopFragment : Fragment(R.layout.fragment_search_top) {

    private val mBinding: FragmentSearchTopBinding by dataBinding()

    @Inject
    internal lateinit var applyMenuTint: ApplyMenuTint

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    val viewModel: SearchTopViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SearchPagerAdapterV2(pageableFragmentFactory, this)

        mBinding.searchViewPager.adapter = adapter
        TabLayoutMediator(
            mBinding.searchTabLayout,
            mBinding.searchViewPager
        ) { tab, position ->
            tab.text = adapter.tabs[position].title.getString(requireContext())
        }.attach()
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_top_menu, menu)
                applyMenuTint(requireActivity(), menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    //R.id.search ->
                    R.id.search -> {
                        activity?.startActivity(
                            Intent(
                                requireContext(),
                                SearchActivity::class.java
                            )
                        )
                        activity?.overridePendingTransition(0, 0)
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.uiState.onEach {
            adapter.submitList(it.tabItems)
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)
    }


    override fun onResume() {
        super.onResume()

        (requireActivity() as? ToolbarSetter)?.apply {
            setToolbar(mBinding.toolbar)
            setTitle(R.string.search)
        }
    }

}

class SearchPagerAdapterV2(
    private val pageableFragmentFactory: PageableFragmentFactory,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    var tabs: List<SearchTopTabItem> = emptyList()
        private set

    override fun getItemCount(): Int {
        return tabs.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = tabs[position]
        return when(item.type) {
            SearchTopTabItem.TabType.MisskeyFeatured -> pageableFragmentFactory.create(Pageable.Featured(null))
            SearchTopTabItem.TabType.MastodonTrends ->  pageableFragmentFactory.create(Pageable.Mastodon.TrendTimeline)
            SearchTopTabItem.TabType.MisskeyExploreUsers -> ExploreFragment.newInstance(ExploreType.Local)
            SearchTopTabItem.TabType.MisskeyExploreFediverseUsers -> ExploreFragment.newInstance(ExploreType.Fediverse)
            SearchTopTabItem.TabType.MastodonUserSuggestions -> ExploreFragment.newInstance(ExploreType.MastodonUserSuggestions)
            SearchTopTabItem.TabType.UserSuggestionByReaction -> ExploreFragment.newInstance(ExploreType.UserSuggestionsByReaction)
            SearchTopTabItem.TabType.HashtagTrend -> TrendFragment()
        }
    }

    fun submitList(list: List<SearchTopTabItem>) {
        val old = tabs
        tabs = list
        val callback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return old.size
            }

            override fun getNewListSize(): Int {
                return list.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == list[newItemPosition]
            }
        }
        val result = DiffUtil.calculateDiff(callback)
        result.dispatchUpdatesTo(this)
    }
}