@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient.ui.notes.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.tabs.TabLayout
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentTabBinding
import jp.panta.misskeyandroidclient.ui.PageableFragmentFactory
import jp.panta.misskeyandroidclient.ui.ScrollableTop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.account.page.Page
import javax.inject.Inject

@AndroidEntryPoint
class TabFragment : Fragment(R.layout.fragment_tab), ScrollableTop {


    private var mPagerAdapter: TimelinePagerAdapter? = null

    private val binding: FragmentTabBinding by dataBinding()

    @Inject
    lateinit var accountStore: AccountStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mPagerAdapter = binding.viewPager.adapter as? TimelinePagerAdapter
        if (mPagerAdapter == null) {
            mPagerAdapter = TimelinePagerAdapter(this.childFragmentManager, emptyList())
            binding.viewPager.adapter = mPagerAdapter
        }

        accountStore.observeCurrentAccount.filterNotNull().flowOn(Dispatchers.IO)
            .onEach { account ->
                val pages = account.pages
                Log.d("TabFragment", "pages:$pages")
                mPagerAdapter?.setList(
                    account,
                    pages.sortedBy {
                        it.weight
                    })
                //mPagerAdapter?.notifyDataSetChanged()
                binding.tabLayout.setupWithViewPager(binding.viewPager)

                if (pages.size <= 1) {
                    binding.tabLayout.visibility = View.GONE
                    binding.elevationView.visibility = View.VISIBLE
                } else {
                    binding.tabLayout.visibility = View.VISIBLE
                    binding.elevationView.visibility = View.GONE
                    binding.tabLayout.elevation
                    if (pages.size > 5) {
                        binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                    } else {
                        binding.tabLayout.tabMode = TabLayout.MODE_FIXED
                    }
                }
            }.launchIn(lifecycleScope)

    }


    class TimelinePagerAdapter(fragmentManager: FragmentManager, list: List<Page>) :
        FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private var requestBaseList: List<Page> = list
        private var oldRequestBaseSetting = requestBaseList

        var account: Account? = null

        val scrollableTopFragments = ArrayList<ScrollableTop>()
        private val mFragments = ArrayList<Fragment>()

        override fun getCount(): Int {
            return requestBaseList.size
        }

        override fun getItem(position: Int): Fragment {
            Log.d("getItem", "$position, ${requestBaseList[position].pageable().javaClass}")
            val item = requestBaseList[position]
            val fragment = PageableFragmentFactory.create(item)

            if (fragment is ScrollableTop) {
                scrollableTopFragments.add(fragment)
            }
            mFragments.add(fragment)
            return fragment
        }


        override fun getPageTitle(position: Int): String {
            val page = requestBaseList[position]
            return page.title
        }

        override fun getItemPosition(any: Any): Int {
            val target = any as Fragment
            if (mFragments.contains(target)) {
                return PagerAdapter.POSITION_UNCHANGED
            }
            return PagerAdapter.POSITION_NONE
        }


        fun setList(account: Account, list: List<Page>) {
            mFragments.clear()
            oldRequestBaseSetting = requestBaseList
            requestBaseList = list
            this.account = account
            if (requestBaseList != oldRequestBaseSetting) {
                notifyDataSetChanged()
            }

        }


    }

    override fun showTop() {
        showTopCurrentFragment()
    }

    private fun showTopCurrentFragment() {
        try {
            mPagerAdapter?.scrollableTopFragments?.forEach {
                it.showTop()
            }
        } catch (e: UninitializedPropertyAccessException) {

        }

    }


}