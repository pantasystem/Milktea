package jp.panta.misskeyandroidclient.ui.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.withBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNotificationBinding
import jp.panta.misskeyandroidclient.ui.ScrollableTop
import jp.panta.misskeyandroidclient.ui.notes.view.NoteCardActionHandler
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notification.viewmodel.NotificationViewData
import jp.panta.misskeyandroidclient.ui.notification.viewmodel.NotificationViewModel
import net.pantasystem.milktea.common_viewmodel.CurrentPageableTimelineViewModel
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject


@AndroidEntryPoint
class NotificationFragment : Fragment(R.layout.fragment_notification), ScrollableTop {


    lateinit var mLinearLayoutManager: LinearLayoutManager
    private val mViewModel: NotificationViewModel by viewModels()

    val notesViewModel by activityViewModels<NotesViewModel>()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    @Inject
    internal lateinit var settingStore: SettingStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(requireContext())

        val adapter = NotificationListAdapter(
            diffUtilItemCallBack,
            notesViewModel,
            mViewModel,
            viewLifecycleOwner
        ) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore
            ).onAction(it)
        }


        withBinding<FragmentNotificationBinding> { mBinding ->
            mBinding.notificationListView.adapter = adapter
            mBinding.notificationListView.layoutManager = mLinearLayoutManager
        }


        mViewModel.notificationsLiveData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        withBinding<FragmentNotificationBinding> { binding ->
            mViewModel.isLoading.observe(viewLifecycleOwner) {
                binding.notificationSwipeRefresh.isRefreshing = it
            }

            binding.notificationSwipeRefresh.setOnRefreshListener {
                mViewModel.loadInit()
            }
            binding.notificationListView.addOnScrollListener(mScrollListener)
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