package net.pantasystem.milktea.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.ScrollToTopViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.notification.databinding.FragmentNotificationBinding
import net.pantasystem.milktea.notification.viewmodel.NotificationListItem
import net.pantasystem.milktea.notification.viewmodel.NotificationViewModel
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification) {


    lateinit var mLinearLayoutManager: LinearLayoutManager
    private val mViewModel: NotificationViewModel by viewModels()
    private val scrollToTopViewModel: ScrollToTopViewModel by activityViewModels()

    private val notesViewModel by activityViewModels<NotesViewModel>()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    @Inject
    internal lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var channelDetailNavigation: ChannelDetailNavigation

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var accountStore: AccountStore

    private val mBinding: FragmentNotificationBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(requireContext())

        val adapter = NotificationListAdapter(
            diffUtilItemCallBack,
            mViewModel,
            viewLifecycleOwner,
            onRetryButtonClicked = {
                mViewModel.loadInit()
            },
            onReauthenticateButtonClicked = {
                startActivity(
                    authorizationNavigation.newIntent(
                        AuthorizationArgs.ReAuth(
                            accountStore.currentAccount
                        )
                    )
                )
            }
        ) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore,
                userDetailNavigation,
                channelDetailNavigation,
            ).onAction(it)
        }


        mViewModel.errors.onEach {
            NotificationErrorHandler(requireContext())(it)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)

        mBinding.notificationListView.adapter = adapter
        mBinding.notificationListView.layoutManager = mLinearLayoutManager

        mViewModel.notifications.onEach {
            adapter.submitList(it)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)


        mViewModel.isLoading.observe(viewLifecycleOwner) {
            mBinding.notificationSwipeRefresh.isRefreshing = it
        }

        mBinding.notificationSwipeRefresh.setOnRefreshListener {
            mViewModel.loadInit()
        }
        mBinding.notificationListView.addOnScrollListener(mScrollListener)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                scrollToTopViewModel.scrollToTopEvent.collect {
                    mBinding.notificationListView.smoothScrollToPosition(0)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        currentPageableTimelineViewModel.setCurrentPageable(Pageable.Notification())
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount


            if (firstVisibleItemPosition == 0) {
                Log.d("", "先頭")
            }

            if (endVisibleItemPosition == (itemCount - 1)) {
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mViewModel.loadOld()

            }

        }
    }

    private val diffUtilItemCallBack = object : DiffUtil.ItemCallback<NotificationListItem>() {

        override fun areContentsTheSame(
            oldItem: NotificationListItem,
            newItem: NotificationListItem
        ): Boolean {
            if (oldItem is NotificationListItem.Notification && newItem is NotificationListItem.Notification) {
                return oldItem.notificationViewData.id == newItem.notificationViewData.id
            }
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: NotificationListItem,
            newItem: NotificationListItem
        ): Boolean {
            if (oldItem is NotificationListItem.Notification && newItem is NotificationListItem.Notification) {
                return oldItem.notificationViewData.id == newItem.notificationViewData.id
            }
            return oldItem == newItem
        }

    }

}