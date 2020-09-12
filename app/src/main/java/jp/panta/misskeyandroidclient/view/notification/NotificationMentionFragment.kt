package jp.panta.misskeyandroidclient.view.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.view.PageableFragmentFactory
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.android.synthetic.main.fragment_notification_mention.*
import kotlinx.android.synthetic.main.fragment_notification_mention.view.*

class NotificationMentionFragment : Fragment(R.layout.fragment_notification_mention){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageableTypeNameMap = PageTypeNameMap(view.context)
        val pagerItems = listOf(
            PageableTemplate.notification(pageableTypeNameMap.get(PageType.NOTIFICATION)),
            PageableTemplate.mention(pageableTypeNameMap.get(PageType.MENTION))
        )

        val notificationPagerAdapter =  PagerAdapter(pagerItems)
        view.notificationPager.adapter = notificationPagerAdapter
        notificationTab.setupWithViewPager(notificationPager)

        val miCore = requireContext().applicationContext as MiApplication
        miCore.mCurrentAccount.observe( viewLifecycleOwner, Observer {
            notificationPagerAdapter.notifyDataSetChanged()
        })

    }

    inner class PagerAdapter(val pages: List<Page>) : FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private fun createFragment(position: Int): Fragment {
            return PageableFragmentFactory.create(null, pages[position].pageable())
        }

        override fun getPageTitle(position: Int): CharSequence? {
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