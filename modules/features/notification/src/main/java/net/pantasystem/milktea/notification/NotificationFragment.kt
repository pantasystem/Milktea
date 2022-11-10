package net.pantasystem.milktea.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.ui.ScrollableTop
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.common_viewmodel.ScrollToTopViewModel
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.notification.databinding.FragmentNotificationBinding
import net.pantasystem.milktea.notification.viewmodel.NotificationViewData
import net.pantasystem.milktea.notification.viewmodel.NotificationViewModel
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification), ScrollableTop {


    lateinit var mLinearLayoutManager: LinearLayoutManager
    private val mViewModel: NotificationViewModel by viewModels()
    private val scrollToTopViewModel: ScrollToTopViewModel by activityViewModels()

    val notesViewModel by activityViewModels<NotesViewModel>()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    @Inject
    internal lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    private val mBinding: FragmentNotificationBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(requireContext())

        val adapter = NotificationListAdapter(
            diffUtilItemCallBack,
            mViewModel,
            viewLifecycleOwner
        ) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore,
                userDetailNavigation
            ).onAction(it)
        }


        mBinding.notificationListView.adapter = adapter
        mBinding.notificationListView.layoutManager = mLinearLayoutManager


        mViewModel.notificationsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

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

    private val diffUtilItemCallBack = object : DiffUtil.ItemCallback<NotificationViewData>() {
        override fun areContentsTheSame(
            oldItem: NotificationViewData,
            newItem: NotificationViewData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: NotificationViewData,
            newItem: NotificationViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun showTop() {
        mLinearLayoutManager.scrollToPosition(0)
    }
}