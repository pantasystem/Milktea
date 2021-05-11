package jp.panta.misskeyandroidclient.view.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNotificationMentionBinding
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.PageType
import jp.panta.misskeyandroidclient.view.PageableFragmentFactory
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class NotificationMentionFragment : Fragment(R.layout.fragment_notification_mention){

    private val mBinding: FragmentNotificationMentionBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageableTypeNameMap = PageTypeNameMap(view.context)
        val pagerItems = listOf(
            PageableTemplate(null).notification(pageableTypeNameMap.get(PageType.NOTIFICATION)),
            PageableTemplate(null).mention(pageableTypeNameMap.get(PageType.MENTION))
        )

        val notificationPagerAdapter =  PagerAdapter(pagerItems)

        mBinding.notificationPager.adapter = notificationPagerAdapter
        mBinding.notificationTab.setupWithViewPager(mBinding.notificationPager)

        val miCore = requireContext().applicationContext as MiApplication
        miCore.getCurrentAccount().filterNotNull().onEach {
            notificationPagerAdapter.notifyDataSetChanged()
        }.launchIn(lifecycleScope)

    }

    inner class PagerAdapter(val pages: List<Page>) : FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private fun createFragment(position: Int): Fragment {
            return PageableFragmentFactory.create(pages[position])
        }

        override fun getPageTitle(position: Int): CharSequence {
            return pages[position].title
        }

        override fun getItem(position: Int): Fragment {
            return createFragment(position)
        }

        override fun getCount(): Int {
            return pages.size
        }



    }
}