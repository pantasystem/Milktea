package jp.panta.misskeyandroidclient.view.notification

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewData
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewModel
import jp.panta.misskeyandroidclient.viewmodel.notification.NotificationViewModelFactory
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NotificationFragment : Fragment(R.layout.fragment_notification), ScrollableTop {


    lateinit var mLinearLayoutManager: LinearLayoutManager
    @ExperimentalCoroutinesApi
    lateinit var mViewModel: NotificationViewModel

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLinearLayoutManager = LinearLayoutManager(requireContext())

        val miApplication = context?.applicationContext as MiApplication

        val notesViewModel = ViewModelProvider(requireActivity(), NotesViewModelFactory(miApplication)).get(NotesViewModel::class.java)

        //val nowConnectionInstance = miApplication.currentConnectionInstanceLiveData.value
        val factory = NotificationViewModelFactory(miApplication)
        mViewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)



        val adapter = NotificationListAdapter(diffUtilItemCallBack, notesViewModel, viewLifecycleOwner)
        notification_list_view.adapter = adapter
        notification_list_view.layoutManager = mLinearLayoutManager

        mViewModel.loadInit()

        mViewModel.notificationsLiveData.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        mViewModel.isLoading.observe(viewLifecycleOwner, Observer {
            notification_swipe_refresh.isRefreshing = it
        })

        notification_swipe_refresh.setOnRefreshListener {
            mViewModel.loadInit()
        }


        notification_list_view.addOnScrollListener(mScrollListener)


    }

    @ExperimentalCoroutinesApi
    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition()
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount


            if(firstVisibleItemPosition == 0){
                Log.d("", "先頭")
            }

            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                //mTimelineViewModel?.getOldTimeline()
                mViewModel.loadOld()

            }

        }
    }

    private val diffUtilItemCallBack = object : DiffUtil.ItemCallback<NotificationViewData>(){
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