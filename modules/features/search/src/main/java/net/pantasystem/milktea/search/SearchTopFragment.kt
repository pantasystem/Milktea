@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Lifecycle
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.search.databinding.FragmentSearchTopBinding
import net.pantasystem.milktea.search.explore.ExploreFragment
import net.pantasystem.milktea.search.explore.ExploreType
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.searchViewPager.adapter =
            SearchPagerAdapter(this.childFragmentManager, requireContext(), pageableFragmentFactory)
        mBinding.searchTabLayout.setupWithViewPager(mBinding.searchViewPager)
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
    }


    override fun onResume() {
        super.onResume()

        (requireActivity() as? ToolbarSetter)?.apply {
            setToolbar(mBinding.toolbar)
            setTitle(R.string.search)
        }
    }


    class SearchPagerAdapter(
        supportFragmentManager: FragmentManager,
        context: Context,
        private val pageableFragmentFactory: PageableFragmentFactory,
    ) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val tabList =
            listOf(
                context.getString(R.string.title_featured),
                context.getString(R.string.explore),
                context.getString(R.string.explore_fediverse)
            )

        override fun getCount(): Int {
            return tabList.size
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> pageableFragmentFactory.create(Pageable.Featured(null))
                1 -> ExploreFragment.newInstance(ExploreType.Local)
                2 -> ExploreFragment.newInstance(ExploreType.Fediverse)
                else -> throw IllegalArgumentException("range 0..1, list:$tabList")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabList[position]
        }
    }
}