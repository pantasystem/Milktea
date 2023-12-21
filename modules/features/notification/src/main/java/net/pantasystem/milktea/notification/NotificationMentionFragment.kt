@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android.resource.getString
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.user.FollowRequestsFragmentFactory
import net.pantasystem.milktea.notification.databinding.FragmentNotificationMentionBinding
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NotificationMentionFragment : Fragment(R.layout.fragment_notification_mention) {

    private val mBinding: FragmentNotificationMentionBinding by dataBinding()
    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    lateinit var followRequestsFragmentFactory: FollowRequestsFragmentFactory

    private val viewModel: NotificationTabViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationPagerAdapter = PagerAdapter()

        mBinding.notificationPager.adapter = notificationPagerAdapter
        mBinding.notificationTab.setupWithViewPager(mBinding.notificationPager)

        viewModel.tabs.onEach {
            notificationPagerAdapter.setList(it)
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle).launchIn(viewLifecycleOwner.lifecycleScope)

    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as? ToolbarSetter?)?.apply {
            setToolbar(mBinding.toolbar)
            setTitle(R.string.notification)
        }

    }
    inner class PagerAdapter() :
        FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private var pages: List<NotificationTabType> = emptyList()

        private fun createFragment(position: Int): Fragment {
            return when(val tabItem = pages[position]) {
                is NotificationTabType.FollowRequests -> {
                    followRequestsFragmentFactory.create()
                }
                is NotificationTabType.TitleWithPageable -> {
                    pageableFragmentFactory.create(tabItem.pageable)
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return pages[position].title.getString(requireContext())
        }

        override fun getItem(position: Int): Fragment {
            return createFragment(position)
        }

        override fun getCount(): Int {
            return pages.size
        }

        fun setList(pages: List<NotificationTabType>) {
            this.pages = pages
            notifyDataSetChanged()
        }

    }
}



