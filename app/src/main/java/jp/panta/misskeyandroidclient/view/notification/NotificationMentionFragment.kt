package jp.panta.misskeyandroidclient.view.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.PageType
import jp.panta.misskeyandroidclient.view.PageableFragmentFactory
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
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
        val mediator = TabLayoutMediator(view.notificationTab, view.notificationPager){ tab: TabLayout.Tab, position: Int ->
            tab.text = notificationPagerAdapter.pages[position].title
        }
        mediator.attach()
    }

    inner class PagerAdapter(val pages: List<Page>) : FragmentStateAdapter(this){

        override fun createFragment(position: Int): Fragment {
            return PageableFragmentFactory.create(null, pages[position].pageable())
        }

        override fun getItemCount(): Int {
            return pages.size
        }

    }
}