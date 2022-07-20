@file:Suppress("DEPRECATION")

package jp.panta.misskeyandroidclient.ui.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNotificationMentionBinding
import jp.panta.misskeyandroidclient.ui.PageableFragmentFactory
import net.pantasystem.milktea.common.ui.ToolbarSetter
import jp.panta.misskeyandroidclient.ui.settings.page.PageTypeNameMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationMentionFragment : Fragment(R.layout.fragment_notification_mention) {

    private val mBinding: FragmentNotificationMentionBinding by dataBinding()
    @Inject
    lateinit var accountStore: AccountStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageableTypeNameMap = PageTypeNameMap(view.context)

        val pagerItems = listOf(
            TitleWithPageable(
                pageableTypeNameMap.get(PageType.NOTIFICATION),
                PageableTemplate(null)
                    .notification(pageableTypeNameMap.get(PageType.NOTIFICATION)).pageable(),
            ),
            TitleWithPageable(
                pageableTypeNameMap.get(PageType.MENTION),
                PageableTemplate(null)
                    .mention(pageableTypeNameMap.get(PageType.MENTION)).pageable(),
            )
        )

        val notificationPagerAdapter = PagerAdapter(pagerItems)

        mBinding.notificationPager.adapter = notificationPagerAdapter
        mBinding.notificationTab.setupWithViewPager(mBinding.notificationPager)


        accountStore.observeCurrentAccount.filterNotNull().onEach {
            notificationPagerAdapter.notifyDataSetChanged()
        }.launchIn(lifecycleScope)

    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as? ToolbarSetter?)?.apply {
            setToolbar(mBinding.toolbar)
            setTitle(R.string.notification)
        }

    }
    inner class PagerAdapter(val pages: List<TitleWithPageable>) :
        FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private fun createFragment(position: Int): Fragment {
            return PageableFragmentFactory.create(pages[position].pageable)
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

data class TitleWithPageable(
    val title: String,
    val pageable: Pageable,
)